package com.picbed.repository;

import com.picbed.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenHashAndIsActiveTrue(String tokenHash);

    Optional<Token> findByTokenHash(String tokenHash);

    List<Token> findByIsActiveTrueAndRevokedFalse();

    List<Token> findByRevokedFalse();

    List<Token> findByIsActiveTrueAndRevokedFalseAndEmailIsNotNull();

    List<Token> findByRevokedFalseAndEmailIsNotNull();

    boolean existsByEmailAndIsActiveTrueAndRevokedFalse(String email);

    List<Token> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByNameAndRevokedFalse(String name);
}

