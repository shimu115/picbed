package com.picbed.job;

import com.picbed.entity.Token;
import com.picbed.service.EmailService;
import com.picbed.service.SettingService;
import com.picbed.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TokenRefreshJob implements Job {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SettingService settingService;

    @Override
    public void execute(JobExecutionContext context) {
        if (!settingService.isAutoRefreshEnabled()) {
            log.debug("Auto refresh is disabled, skipping token refresh job");
            return;
        }
        log.info("Token refresh job started");
        List<Token> tokens = tokenService.findAllActive();
        if (tokens.isEmpty()) {
            log.info("No active tokens to process");
            return;
        }
        int refreshed = 0;
        int revoked = 0;
        for (Token token : tokens) {
            try {
                if (token.getEmail() != null && !token.getEmail().isBlank()) {
                    String newRawToken = tokenService.refreshToken(token.getId());
                    emailService.sendTokenRefresh(token.getEmail(), token.getName(), newRawToken);
                    refreshed++;
                } else {
                    tokenService.revokeById(token.getId());
                    log.info("Revoked token '{}' (id={}): no email set", token.getName(), token.getId());
                    revoked++;
                }
            } catch (Exception e) {
                log.error("Failed to process token id={}: {}", token.getId(), e.getMessage());
            }
        }
        log.info("Token refresh job completed: refreshed {}, revoked {}", refreshed, revoked);
    }
}
