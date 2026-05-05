package dev.tmmc.ulms.jobs;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.Notification;
import dev.tmmc.ulms.objects.entities.Reservation;
import dev.tmmc.ulms.objects.entities.SchedulerRun;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.NotificationType;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.repositories.NotificationRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.repositories.SchedulerRunRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.NotificationService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class NotificationDispatchJobTest {

    @Autowired private NotificationDispatchJob job;
    @Autowired private LoanRepository loanRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private FineRepository fineRepository;
    @Autowired private SchedulerRunRepository schedulerRunRepository;
    @Autowired private NotificationService notificationService;

    @BeforeEach
    void cleanDatabase() {
        schedulerRunRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
        fineRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        loanRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void sendsDueReminderForLoansDueInThreeDays() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        loanRepository.saveAndFlush(TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(3)));

        job.dispatchAll();

        List<Notification> all = notificationRepository.findAll();
        assertEquals(1, all.size());
        assertEquals(NotificationType.DUE_REMINDER, all.get(0).getType());
    }

    @Test
    void skipsDueReminderIfAlreadySent() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(
                TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(3)));
        notificationService.createNotification(student, loan, NotificationType.DUE_REMINDER, "earlier reminder");

        job.dispatchAll();

        List<Notification> all = notificationRepository.findAll();
        assertEquals(1, all.size(), "no duplicate due reminder should be created");
    }

    @Test
    void sendsOverdueAlertForOverdueLoanWithoutPriorAlert() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        loanRepository.saveAndFlush(TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().minusDays(2)));

        job.dispatchAll();

        long overdueCount = notificationRepository.findAll().stream()
                .filter(n -> n.getType() == NotificationType.OVERDUE_ALERT)
                .count();
        assertEquals(1, overdueCount);
    }

    @Test
    void skipsOverdueAlertIfAlreadySent() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(
                TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().minusDays(2)));
        notificationService.createNotification(student, loan, NotificationType.OVERDUE_ALERT, "first alert");

        job.dispatchAll();

        long overdueCount = notificationRepository.findAll().stream()
                .filter(n -> n.getType() == NotificationType.OVERDUE_ALERT)
                .count();
        assertEquals(1, overdueCount, "no duplicate overdue alert should be created");
    }

    @Test
    void sendsReservationReadyOnlyWhenAvailableAndNotPreviouslyNotified() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 1));
        reservationRepository.saveAndFlush(
                TestFixtures.reservation(student, book, ReservationStatus.ACTIVE));

        job.dispatchAll();
        job.dispatchAll();

        long readyCount = notificationRepository.findAll().stream()
                .filter(n -> n.getType() == NotificationType.RESERVATION_READY)
                .count();
        assertEquals(1, readyCount, "duplicate run must not create another reservation-ready notification");

        Reservation updated = reservationRepository.findAll().get(0);
        assertNotNull(updated.getNotified_at(), "notified_at should be stamped");
    }

    @Test
    void recordsSchedulerRunRowWithSummedCount() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book dueBook = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Book overdueBook = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Book reservedBook = bookRepository.saveAndFlush(TestFixtures.book(1, 1));

        loanRepository.saveAndFlush(TestFixtures.loan(student, dueBook, LoanStatus.ACTIVE, LocalDate.now().plusDays(3)));
        loanRepository.saveAndFlush(TestFixtures.loan(student, overdueBook, LoanStatus.ACTIVE, LocalDate.now().minusDays(1)));
        reservationRepository.saveAndFlush(TestFixtures.reservation(student, reservedBook, ReservationStatus.ACTIVE));

        job.dispatchAll();

        List<SchedulerRun> runs = schedulerRunRepository.findAll();
        assertEquals(1, runs.size());
        SchedulerRun run = runs.get(0);
        assertEquals("notification-dispatch", run.getJobName());
        assertEquals(3, run.getItemsProcessed(), "due + overdue + reservation = 3");
        assertTrue(run.getFinishedAt().isAfter(run.getStartedAt().minusSeconds(1)));
    }
}
