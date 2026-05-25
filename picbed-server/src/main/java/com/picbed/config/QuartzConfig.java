package com.picbed.config;

import com.picbed.job.TokenRefreshJob;
import com.picbed.service.SettingService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class QuartzConfig {

    private static final String JOB_KEY = "tokenRefreshJob";
    private static final String TRIGGER_KEY = "tokenRefreshTrigger";

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SettingService settingService;

    @PostConstruct
    public void init() {
        try {
            scheduleWithCron(settingService.getTokenRefreshCron());
            log.info("Token refresh job scheduled with cron: {}", settingService.getTokenRefreshCron());
        } catch (SchedulerException e) {
            log.error("Failed to schedule token refresh job", e);
        }
    }

    public void reschedule(String cronExpression) throws SchedulerException {
        scheduleWithCron(cronExpression);
        log.info("Token refresh job rescheduled with cron: {}", cronExpression);
    }

    private void scheduleWithCron(String cronExpression) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(TokenRefreshJob.class)
                .withIdentity(JOB_KEY)
                .storeDurably()
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_KEY)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionDoNothing())
                .build();

        if (scheduler.checkExists(TriggerKey.triggerKey(TRIGGER_KEY))) {
            scheduler.rescheduleJob(TriggerKey.triggerKey(TRIGGER_KEY), trigger);
        } else {
            scheduler.scheduleJob(jobDetail, trigger);
        }
    }
}
