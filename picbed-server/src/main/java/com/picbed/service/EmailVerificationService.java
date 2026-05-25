package com.picbed.service;

import com.picbed.entity.EmailAttemptLog;
import com.picbed.entity.EmailVerification;
import com.picbed.repository.EmailAttemptLogRepository;
import com.picbed.repository.EmailVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int CODE_VALID_MINUTES = 3;
    private static final int RATE_LIMIT_MINUTES = 30;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private EmailVerificationRepository verificationRepository;

    @Autowired
    private EmailAttemptLogRepository attemptLogRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Transactional
    public void sendCode(Long tokenId, String email) {
        checkRateLimit(tokenId);

        verificationRepository.deleteByEmail(email);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        EmailVerification ev = new EmailVerification();
        ev.setEmail(email.trim().toLowerCase());
        ev.setCode(code);
        ev.setTokenId(tokenId);
        ev.setCreatedAt(LocalDateTime.now());
        verificationRepository.save(ev);

        EmailAttemptLog logEntry = new EmailAttemptLog();
        logEntry.setTokenId(tokenId);
        logEntry.setCreatedAt(LocalDateTime.now());
        attemptLogRepository.save(logEntry);

        if (mailSender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromMail);
                msg.setTo(email.trim());
                msg.setSubject("PicBed 邮箱验证码");
                msg.setText("您的 PicBed 邮箱验证码为：" + code + "\n\n"
                        + "验证码 " + CODE_VALID_MINUTES + " 分钟内有效，请勿泄露给他人。");
                mailSender.send(msg);
            } catch (Exception e) {
                log.error("Failed to send verification code to {}: {}", email, e.getMessage());
            }
        }

        log.info("Sent verification code to {} for token id={}", email, tokenId);
    }

    @Transactional
    public void verifyCode(Long tokenId, String email, String code) {
        checkRateLimit(tokenId);

        EmailVerification ev = verificationRepository.findById(email.trim().toLowerCase()).orElse(null);
        if (ev == null || !ev.getCode().equals(code)) {
            recordFailedAttempt(tokenId);
            throw new IllegalArgumentException("验证码错误");
        }

        if (ev.getCreatedAt().plusMinutes(CODE_VALID_MINUTES).isBefore(LocalDateTime.now())) {
            verificationRepository.delete(ev);
            recordFailedAttempt(tokenId);
            throw new IllegalArgumentException("验证码已过期，请重新发送");
        }

        verificationRepository.delete(ev);
        log.info("Email {} verified for token id={}", email, tokenId);
    }

    private void checkRateLimit(Long tokenId) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(RATE_LIMIT_MINUTES);
        int count = attemptLogRepository.countRecentAttempts(tokenId, since);
        if (count >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException("验证次数过多，请30分钟后再尝试发送验证码");
        }
    }

    private void recordFailedAttempt(Long tokenId) {
        EmailAttemptLog logEntry = new EmailAttemptLog();
        logEntry.setTokenId(tokenId);
        logEntry.setCreatedAt(LocalDateTime.now());
        attemptLogRepository.save(logEntry);
    }
}
