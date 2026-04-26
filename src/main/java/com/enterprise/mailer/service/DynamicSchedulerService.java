package com.enterprise.mailer.service;

import com.enterprise.mailer.model.ReportConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class DynamicSchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final ReportConfigService configService;
    private final ReportExecutionService executionService;
    
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public DynamicSchedulerService(ThreadPoolTaskScheduler taskScheduler, ReportConfigService configService, @Lazy ReportExecutionService executionService) {
        this.taskScheduler = taskScheduler;
        this.configService = configService;
        this.executionService = executionService;
    }

    @PostConstruct
    public void init() {
        reloadAll();
    }

    public void reloadAll() {
        log.info("Reloading all active report schedules...");
        // Cancel existing
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();

        List<ReportConfig> activeConfigs = configService.getActiveConfigs();
        for (ReportConfig config : activeConfigs) {
            scheduleTask(config);
        }
    }

    public void scheduleTask(ReportConfig config) {
        if (config == null || config.getId() == null) {
            return;
        }

        // Default to true if active is null
        boolean isActive = config.getActive() == null || config.getActive();

        if (!isActive) {
            cancelTask(config.getId());
            return;
        }

        cancelTask(config.getId()); // Cancel if exists

        try {
            CronTrigger cronTrigger = new CronTrigger(config.getCronExpression());
            ScheduledFuture<?> future = taskScheduler.schedule(() -> {
                try {
                    log.info("Triggering scheduled report config id: {}", config.getId());
                    executionService.executeReport(config.getId());
                } catch (Throwable e) {
                    log.error("Fatal error in scheduler thread for config id {}: {}", config.getId(), e.getMessage(), e);
                }
            }, cronTrigger);
            
            if (future != null) {
                scheduledTasks.put(config.getId(), future);
                log.info("Scheduled report config id: {} with cron: {}", config.getId(), config.getCronExpression());
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression for config id {}: {}", config.getId(), config.getCronExpression(), e);
        }
    }

    public void cancelTask(Long configId) {
        if (configId == null) return;
        ScheduledFuture<?> future = scheduledTasks.remove(configId);
        if (future != null) {
            future.cancel(false);
            log.info("Cancelled schedule for report config id: {}", configId);
        }
    }
}
