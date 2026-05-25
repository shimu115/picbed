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
}
