package com.picbed.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenResponse {

    private Long id;

    private String name;

    private String role;

    private String email;

    private String token;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}
