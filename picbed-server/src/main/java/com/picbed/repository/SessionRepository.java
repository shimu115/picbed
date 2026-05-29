package com.picbed.repository;

import com.picbed.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

    Optional<Session> findBySessionIdAndRevokedFalse(String sessionId);

    @Modifying
    @Transactional
    @Query("UPDATE Session s SET s.revoked = true WHERE s.tokenId = :tokenId AND s.revoked = false")
    void revokeByTokenId(@Param("tokenId") Long tokenId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Session s WHERE s.revoked = true OR s.lastSeenAt < :cutoff")
    void deleteExpiredSessions(@Param("cutoff") LocalDateTime cutoff);
}
