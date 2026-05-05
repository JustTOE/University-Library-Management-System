package dev.tmmc.ulms.jobs;

import dev.tmmc.ulms.objects.entities.SchedulerRun;
import dev.tmmc.ulms.objects.entities.enums.SchedulerRunStatus;
import dev.tmmc.ulms.objects.repositories.SchedulerRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.function.IntSupplier;

@Component
public class SchedulerRunRecorder {

    private static final Logger log = LoggerFactory.getLogger(SchedulerRunRecorder.class);

    private final SchedulerRunRepository runs;

    public SchedulerRunRecorder(SchedulerRunRepository runs) {
        this.runs = runs;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int record(String jobName, IntSupplier work) {
        SchedulerRun run = new SchedulerRun();
        run.setJobName(jobName);
        run.setStartedAt(OffsetDateTime.now());
        run.setStatus(SchedulerRunStatus.OK);
        run = runs.saveAndFlush(run);

        try {
            int processed = work.getAsInt();
            run.setItemsProcessed(processed);
            run.setFinishedAt(OffsetDateTime.now());
            run.setStatus(SchedulerRunStatus.OK);
            runs.save(run);
            log.info("scheduler_run job={} status=OK items={}", jobName, processed);
            return processed;
        } catch (RuntimeException ex) {
            run.setFinishedAt(OffsetDateTime.now());
            run.setStatus(SchedulerRunStatus.FAIL);
            String summary = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            run.setErrorMessage(summary.length() > 1900 ? summary.substring(0, 1900) : summary);
            runs.save(run);
            log.error("scheduler_run job={} status=FAIL", jobName, ex);
            throw ex;
        }
    }
}
