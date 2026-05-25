package com.picbed.job;

import com.picbed.entity.Token;
import com.picbed.service.EmailService;
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

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Token refresh job started");
        List<Token> tokens = tokenService.findAllActiveWithEmail();
        if (tokens.isEmpty()) {
            log.info("No tokens with email to refresh");
            return;
        }
        int count = 0;
        for (Token token : tokens) {
            try {
                String newRawToken = tokenService.refreshToken(token.getId());
                emailService.sendTokenRefresh(token.getEmail(), token.getName(), newRawToken);
                count++;
            } catch (Exception e) {
                log.error("Failed to refresh token id={}: {}", token.getId(), e.getMessage());
            }
        }
        log.info("Token refresh job completed: refreshed {}/{} tokens", count, tokens.size());
    }
}
