package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.SchedulerRun;
import dev.tmmc.ulms.objects.entities.enums.SchedulerRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchedulerRunRepository extends JpaRepository<SchedulerRun, Integer> {

    Optional<SchedulerRun> findFirstByJobNameAndStatusOrderByFinishedAtDesc(
            String jobName, SchedulerRunStatus status);
}
