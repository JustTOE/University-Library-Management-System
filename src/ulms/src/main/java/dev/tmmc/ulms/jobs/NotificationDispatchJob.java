package dev.tmmc.ulms.jobs;

import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.Reservation;
import dev.tmmc.ulms.objects.entities.enums.NotificationType;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.repositories.NotificationRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.services.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class NotificationDispatchJob {

    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final SchedulerRunRecorder recorder;
    private final int dueReminderDaysAhead;

    public NotificationDispatchJob(LoanRepository loanRepository,
                                   ReservationRepository reservationRepository,
                                   NotificationRepository notificationRepository,
                                   NotificationService notificationService,
                                   SchedulerRunRecorder recorder,
                                   @Value("${ulms.scheduler.due-reminder-days-ahead:3}") int dueReminderDaysAhead) {
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.recorder = recorder;
        this.dueReminderDaysAhead = dueReminderDaysAhead;
    }

    @Scheduled(cron = "${ulms.scheduler.notification-cron:0 5 2 * * *}",
               zone = "${ulms.scheduler.zone:Europe/Bucharest}")
    public void dispatchAll() {
        recorder.record("notification-dispatch", () -> {
            int dueCount = dispatchDueReminders();
            int overdueCount = dispatchOverdueAlerts();
            int reservationCount = dispatchReservationReady();
            return dueCount + overdueCount + reservationCount;
        });
    }

    private int dispatchDueReminders() {
        Date target = Date.valueOf(LocalDate.now().plusDays(dueReminderDaysAhead));
        List<Loan> loans = loanRepository.findDueOnDate(target);
        int sent = 0;
        for (Loan loan : loans) {
            if (notificationRepository.existsByLoanAndType(loan, NotificationType.DUE_REMINDER)) {
                continue;
            }
            String message = "Reminder: \"" + loan.getBook().getTitle()
                    + "\" is due on " + loan.getDue_date() + ".";
            notificationService.createNotification(loan.getUser(), loan, NotificationType.DUE_REMINDER, message);
            sent++;
        }
        return sent;
    }

    private int dispatchOverdueAlerts() {
        Date today = Date.valueOf(LocalDate.now());
        List<Loan> loans = loanRepository.findCandidatesForFineCalculation(today);
        int sent = 0;
        for (Loan loan : loans) {
            if (notificationRepository.existsByLoanAndType(loan, NotificationType.OVERDUE_ALERT)) {
                continue;
            }
            String message = "Overdue: \"" + loan.getBook().getTitle()
                    + "\" was due on " + loan.getDue_date() + ".";
            notificationService.createNotification(loan.getUser(), loan, NotificationType.OVERDUE_ALERT, message);
            sent++;
        }
        return sent;
    }

    private int dispatchReservationReady() {
        List<Reservation> ready = reservationRepository.findReadyForNotification(ReservationStatus.ACTIVE);
        int sent = 0;
        for (Reservation reservation : ready) {
            String message = "Your reservation for \"" + reservation.getBook().getTitle()
                    + "\" is ready to borrow.";
            notificationService.createNotification(reservation.getUser(), null,
                    NotificationType.RESERVATION_READY, message);
            reservation.setNotified_at(OffsetDateTime.now());
            reservationRepository.save(reservation);
            sent++;
        }
        return sent;
    }
}
