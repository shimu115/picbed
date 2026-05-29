package com.picbed.config;

import com.picbed.job.SessionCleanupJob;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SessionCleanupConfig {

    @Autowired
    private QuartzConfig quartzConfig;

    @PostConstruct
    public void init() {
        try {
            quartzConfig.scheduleJob(SessionCleanupJob.class, "sessionCleanup", "0 */15 * * * ?");
        } catch (SchedulerException e) {
            log.error("Failed to schedule session cleanup job", e);
        }
    }
}
