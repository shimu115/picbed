package com.picbed.repository;

import com.picbed.entity.EmailAttemptLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EmailAttemptLogRepository extends JpaRepository<EmailAttemptLog, Long> {

    @Query("SELECT COUNT(e) FROM EmailAttemptLog e WHERE e.tokenId = :tokenId AND e.createdAt > :since")
    int countRecentAttempts(@Param("tokenId") Long tokenId, @Param("since") LocalDateTime since);
}
