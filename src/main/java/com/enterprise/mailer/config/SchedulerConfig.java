package com.enterprise.mailer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        threadPoolTaskScheduler.setThreadNamePrefix("ReportScheduler-");
        threadPoolTaskScheduler.setErrorHandler(t -> {
            System.err.println("Unexpected error occurred in scheduled task: " + t.getMessage());
            t.printStackTrace();
        });
        return threadPoolTaskScheduler;
    }
}
