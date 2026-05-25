package com.picbed.repository;

import com.picbed.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenHashAndIsActiveTrue(String tokenHash);

    Optional<Token> findByTokenHash(String tokenHash);

    List<Token> findByIsActiveTrue();

    long countByIsActiveTrueAndRole(String role);

    List<Token> findByIsActiveTrueAndEmailIsNotNull();
}
