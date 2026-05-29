package com.picbed.service;

import com.picbed.entity.Session;
import com.picbed.repository.SessionRepository;
import com.picbed.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class SessionService {

    private static final int ADMIN_INACTIVITY_MINUTES = 30;
    private static final int ADMIN_ABSOLUTE_MINUTES = 720;
    private static final int USER_INACTIVITY_MINUTES = 10080;  // 7 days
    private static final int USER_ABSOLUTE_MINUTES = 43200;    // 30 days

    public static final int COOKIE_MAX_AGE_SECONDS = 2592000;  // 30 days

    @Autowired
    private SessionRepository sessionRepository;

    @Transactional
    public Session createSession(Long tokenId, String role) {
        String sessionId = TokenUtil.generateRawToken();
        Session session = new Session();
        session.setSessionId(sessionId);
        session.setTokenId(tokenId);
        session.setRole(role != null ? role.toUpperCase() : "USER");
        session.setCreatedAt(LocalDateTime.now());
        session.setLastSeenAt(LocalDateTime.now());
        session.setRevoked(false);
        sessionRepository.save(session);
        log.info("Created session {} for tokenId={} role={}", sessionId, tokenId, session.getRole());
        return session;
    }

    public Optional<Session> validateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        Optional<Session> opt = sessionRepository.findBySessionIdAndRevokedFalse(sessionId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Session session = opt.get();

        int inactivityMinutes, absoluteMinutes;
        if ("ADMIN".equalsIgnoreCase(session.getRole())) {
            inactivityMinutes = ADMIN_INACTIVITY_MINUTES;
            absoluteMinutes = ADMIN_ABSOLUTE_MINUTES;
        } else {
            inactivityMinutes = USER_INACTIVITY_MINUTES;
            absoluteMinutes = USER_ABSOLUTE_MINUTES;
        }

        LocalDateTime now = LocalDateTime.now();
        if (session.getLastSeenAt().plusMinutes(inactivityMinutes).isBefore(now)) {
            log.info("Session {} expired due to inactivity (last seen: {})", sessionId, session.getLastSeenAt());
            session.setRevoked(true);
            sessionRepository.save(session);
            return Optional.empty();
        }
        if (session.getCreatedAt().plusMinutes(absoluteMinutes).isBefore(now)) {
            log.info("Session {} expired due to absolute timeout (created: {})", sessionId, session.getCreatedAt());
            session.setRevoked(true);
            sessionRepository.save(session);
            return Optional.empty();
        }

        session.setLastSeenAt(now);
        sessionRepository.save(session);
        return Optional.of(session);
    }

    @Transactional
    public void revokeSessionsByTokenId(Long tokenId) {
        sessionRepository.revokeByTokenId(tokenId);
        log.info("Revoked all sessions for tokenId={}", tokenId);
    }

    @Transactional
    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Deleted session {}", sessionId);
    }

    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        sessionRepository.deleteExpiredSessions(cutoff);
        log.debug("Cleaned up expired sessions older than {}", cutoff);
    }
}
