package dev.tmmc.ulms.jobs;

import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.services.FineService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class FineCalculationJob {

    private final LoanRepository loanRepository;
    private final FineService fineService;
    private final SchedulerRunRecorder recorder;

    public FineCalculationJob(LoanRepository loanRepository,
                              FineService fineService,
                              SchedulerRunRecorder recorder) {
        this.loanRepository = loanRepository;
        this.fineService = fineService;
        this.recorder = recorder;
    }

    @Scheduled(cron = "${ulms.scheduler.fine-cron:0 0 2 * * *}",
               zone = "${ulms.scheduler.zone:Europe/Bucharest}")
    public void recalculateOverdueFines() {
        recorder.record("fine-calculation", () -> {
            Date today = Date.valueOf(LocalDate.now());
            List<Loan> overdue = loanRepository.findCandidatesForFineCalculation(today);
            overdue.forEach(fineService::calculateFine);
            return overdue.size();
        });
    }
}
