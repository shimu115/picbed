package com.picbed.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class QuartzConfig {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void init() {
        try {
            if (!scheduler.isStarted()) {
                scheduler.start();
                log.info("Quartz scheduler started");
            }
        } catch (SchedulerException e) {
            log.error("Failed to start Quartz scheduler", e);
        }
    }

    public void scheduleJob(Class<? extends Job> jobClass, String jobKey, String cronExpression) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .storeDurably()
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobKey + "Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionDoNothing())
                .build();

        if (scheduler.checkExists(TriggerKey.triggerKey(jobKey + "Trigger"))) {
            scheduler.rescheduleJob(TriggerKey.triggerKey(jobKey + "Trigger"), trigger);
        } else {
            scheduler.scheduleJob(jobDetail, trigger);
        }

        log.info("Scheduled job '{}' with cron: {}", jobKey, cronExpression);
    }

    public void reschedule(String jobKey, String cronExpression) throws SchedulerException {
        scheduleJob(getExistingJobClass(jobKey), jobKey, cronExpression);
    }

    public void deleteJob(String jobKey) throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey(jobKey));
        log.info("Deleted job '{}'", jobKey);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Job> getExistingJobClass(String jobKey) throws SchedulerException {
        JobDetail detail = scheduler.getJobDetail(JobKey.jobKey(jobKey));
        if (detail == null) {
            throw new SchedulerException("Job not found: " + jobKey);
        }
        return (Class<? extends Job>) detail.getJobClass();
    }
}
