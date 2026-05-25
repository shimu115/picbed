package com.picbed.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public boolean isConfigured() {
        return mailSender != null;
    }

    public void sendTokenRefresh(String to, String tokenName, String newToken) {
        if (mailSender == null) {
            log.warn("Mail not configured, skipping email to {}", to);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject("PicBed Token Refreshed: " + tokenName);
            msg.setText("Your PicBed token \"" + tokenName + "\" has been refreshed.\n\n"
                    + "New token (save it now — it will not be shown again):\n"
                    + newToken + "\n\n"
                    + "The old token is now invalid.");
            mailSender.send(msg);
            log.info("Sent token refresh email to {} for token '{}'", to, tokenName);
        } catch (Exception e) {
            log.error("Failed to send token refresh email to {}: {}", to, e.getMessage());
        }
    }

    public void sendTokenCompromisedWarning(String to, String tokenName) {
        if (mailSender == null) {
            log.warn("Mail not configured, skipping warning email to {}", to);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject("PicBed Token Security Warning: " + tokenName);
            msg.setText("Your PicBed token \"" + tokenName + "\" may have been compromised.\n\n"
                    + "Please bind an email address and manually refresh your token immediately "
                    + "to prevent unauthorized access. If you do not take action, your token "
                    + "will be revoked during the next automatic refresh cycle.\n\n"
                    + "To secure your token: log in and use the refresh function in settings.");
            mailSender.send(msg);
            log.info("Sent token compromised warning to {} for token '{}'", to, tokenName);
        } catch (Exception e) {
            log.error("Failed to send warning email to {}: {}", to, e.getMessage());
        }
    }
}
