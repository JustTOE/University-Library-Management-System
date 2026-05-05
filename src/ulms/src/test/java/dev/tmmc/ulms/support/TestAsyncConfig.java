package dev.tmmc.ulms.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@TestConfiguration
public class TestAsyncConfig {

    @Bean(name = "ulmsTaskExecutor")
    @Primary
    public TaskExecutor ulmsTaskExecutor() {
        return new SyncTaskExecutor();
    }
}
