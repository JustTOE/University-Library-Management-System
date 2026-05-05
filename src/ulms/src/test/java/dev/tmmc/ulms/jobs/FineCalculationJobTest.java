package dev.tmmc.ulms.jobs;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.SchedulerRun;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.SchedulerRunStatus;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.repositories.SchedulerRunRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
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

@SpringBootTest
@ActiveProfiles("test")
class FineCalculationJobTest {

    @Autowired private FineCalculationJob job;
    @Autowired private LoanRepository loanRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private FineRepository fineRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SchedulerRunRepository schedulerRunRepository;

    @BeforeEach
    void cleanDatabase() {
        schedulerRunRepository.deleteAllInBatch();
        fineRepository.deleteAllInBatch();
        loanRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void processesOnlyOverdueActiveLoansAndCreatesFines() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book overdueBook = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Book returnedBook = bookRepository.saveAndFlush(TestFixtures.book(1, 1));
        Book futureBook = bookRepository.saveAndFlush(TestFixtures.book(1, 0));

        loanRepository.saveAndFlush(TestFixtures.loan(student, overdueBook, LoanStatus.ACTIVE, LocalDate.now().minusDays(3)));
        loanRepository.saveAndFlush(TestFixtures.loan(student, returnedBook, LoanStatus.RETURNED, LocalDate.now().minusDays(5)));
        loanRepository.saveAndFlush(TestFixtures.loan(student, futureBook, LoanStatus.ACTIVE, LocalDate.now().plusDays(2)));

        job.recalculateOverdueFines();

        assertEquals(1, fineRepository.findAll().size(), "only the active overdue loan should produce a fine");
    }

    @Test
    void runIsIdempotentWhenCalledTwice() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        loanRepository.saveAndFlush(TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().minusDays(2)));

        job.recalculateOverdueFines();
        job.recalculateOverdueFines();

        assertEquals(1, fineRepository.findAll().size(), "duplicate run must not create another fine");
        long okRuns = schedulerRunRepository.findAll().stream()
                .filter(r -> r.getStatus() == SchedulerRunStatus.OK)
                .count();
        assertEquals(2, okRuns, "two OK runs should be recorded");
    }

    @Test
    void recordsSchedulerRunRowOnSuccess() {
        User student = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        loanRepository.saveAndFlush(TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().minusDays(1)));

        job.recalculateOverdueFines();

        List<SchedulerRun> runs = schedulerRunRepository.findAll();
        assertEquals(1, runs.size());
        SchedulerRun run = runs.get(0);
        assertEquals("fine-calculation", run.getJobName());
        assertEquals(SchedulerRunStatus.OK, run.getStatus());
        assertEquals(1, run.getItemsProcessed());
        assertNotNull(run.getStartedAt());
        assertNotNull(run.getFinishedAt());
    }

    @Test
    void emptyRunStillRecordsAuditRow() {
        job.recalculateOverdueFines();

        assertEquals(0, fineRepository.findAll().size());
        assertEquals(1, schedulerRunRepository.findAll().size());
        assertEquals(0, schedulerRunRepository.findAll().get(0).getItemsProcessed());
    }
}
