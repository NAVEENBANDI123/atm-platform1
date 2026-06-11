package com.atm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Async} (used by the email pipeline) and {@code @Scheduled}
 * (used by the email outbox flusher and scheduled-transfer runner).  The
 * underlying executor is the Spring Boot {@code applicationTaskExecutor}
 * configured in {@code application.yml} under {@code spring.task.execution}.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
