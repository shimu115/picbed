package com.picbed.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    @Value("${spring.mail.username:}")
    private String fromAddress;

    public boolean isConfigured() {
        return mailSender != null;
    }

    public void sendTokenCreated(String to, String tokenName, String token) {
        if (mailSender == null) {
            log.warn("Mail not configured, skipping setup email to {}", to);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setFrom(fromAddress);
            msg.setSubject("PicBed Token 已创建: " + tokenName);
            msg.setText("您的 PicBed Token \"" + tokenName + "\" 已创建成功。\n\n"
                    + "Token（请妥善保管，关闭后将无法再次查看）：\n"
                    + token + "\n\n"
                    + "请使用此 Token 登录 PicBed 并开始使用。");
            mailSender.send(msg);
            log.info("Sent token creation email to {} for token '{}'", to, tokenName);
        } catch (Exception e) {
            log.error("Failed to send token creation email to {}: {}", to, e.getMessage());
        }
    }

    public void sendTokenCompromisedWarning(String to, String tokenName) {
        if (mailSender == null) {
            log.warn("Mail not configured, skipping warning email to {}", to);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject("PicBed Token 安全提醒: " + tokenName);
            msg.setText("您的 PicBed Token \"" + tokenName + "\" 可能存在安全风险。\n\n"
                    + "请尽快绑定邮箱并手动刷新 Token，以防止未经授权的访问。"
                    + "若未及时处理，您的 Token 将在下次自动刷新时被吊销。\n\n"
                    + "请登录后在设置中使用刷新功能来保护您的 Token。");
            mailSender.send(msg);
            log.info("Sent token compromised warning to {} for token '{}'", to, tokenName);
        } catch (Exception e) {
            log.error("Failed to send warning email to {}: {}", to, e.getMessage());
        }
    }
}
