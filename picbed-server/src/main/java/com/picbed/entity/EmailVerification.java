package com.picbed.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @Column(length = 255)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(name = "token_id", nullable = false)
    private Long tokenId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
