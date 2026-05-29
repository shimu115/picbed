package com.picbed.job;

import com.picbed.service.SessionService;
import com.picbed.util.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public class SessionCleanupJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Running session cleanup job");
        try {
            ApplicationContextHolder.getBean(SessionService.class).cleanupExpiredSessions();
        } catch (Exception e) {
            log.error("Session cleanup job failed", e);
        }
    }
}
