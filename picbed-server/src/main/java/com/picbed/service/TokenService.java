package com.picbed.service;

import com.picbed.dto.TokenResponse;
import com.picbed.entity.Token;
import com.picbed.repository.TokenRepository;
import com.picbed.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private SessionService sessionService;

    @Transactional
    public TokenResponse createToken(String name, String role, String email) {
        if (tokenRepository.existsByNameAndRevokedFalse(name)) {
            throw new IllegalArgumentException("Username '" + name + "' already exists");
        }

        String rawToken = TokenUtil.generateRawToken();
        String tokenHash = TokenUtil.hashToken(rawToken);

        String resolvedRole = (role != null && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("USER")))
                ? role.toUpperCase() : "USER";

        Token token = new Token();
        token.setName(name);
        token.setTokenHash(tokenHash);
        token.setRole(resolvedRole);
        token.setIsActive(true);
        if (email != null && !email.isBlank()) {
            token.setEmail(email.trim());
        }
        tokenRepository.save(token);

        log.info("Created {} token '{}' (id={})", resolvedRole, name, token.getId());

        TokenResponse result = new TokenResponse();
        result.setId(token.getId());
        result.setName(token.getName());
        result.setRole(token.getRole());
        result.setEmail(token.getEmail());
        result.setToken(rawToken);
        result.setCreatedAt(token.getCreatedAt());
        return result;
    }

    public String validateTokenStatus(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return "invalid";
        }
        String hash = TokenUtil.hashToken(rawToken);
        Optional<Token> opt = tokenRepository.findByTokenHash(hash);
        if (opt.isEmpty()) {
            return "invalid";
        }
        Token token = opt.get();
        if (token.getRevoked()) {
            return "revoked";
        }
        if (!token.getIsActive()) {
            return "disabled";
        }
        return "valid";
    }

    public Optional<Token> findByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return tokenRepository.findByTokenHash(TokenUtil.hashToken(rawToken));
    }

    @Transactional
    public void updateEmail(Long tokenId, String email) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found: " + tokenId));
        String normalized = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : null;
        if (normalized != null && tokenRepository.existsByEmailAndIsActiveTrueAndRevokedFalse(normalized)
                && !normalized.equals(token.getEmail())) {
            throw new IllegalArgumentException("邮箱已被其他用户使用，请换一个邮箱");
        }
        token.setEmail(normalized);
        tokenRepository.save(token);
        log.info("Updated email for token '{}' (id={})", token.getName(), token.getId());
    }

    public List<Token> findAllActive() {
        return tokenRepository.findByIsActiveTrueAndRevokedFalse();
    }

    @Transactional
    public void revokeById(Long id) {
        Token token = tokenRepository.findById(id).orElse(null);
        if (token != null && token.getIsActive()) {
            token.setIsActive(false);
            tokenRepository.save(token);
            log.info("System revoked token '{}' (id={})", token.getName(), id);
        }
    }

    @Transactional
    public void toggleTokenActive(Long id, boolean active, Long requesterTokenId) {
        Token token = tokenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Token not found: " + id));
        if (token.getRevoked()) {
            throw new IllegalArgumentException("Cannot toggle a revoked token");
        }
        if ("ADMIN".equalsIgnoreCase(token.getRole())) {
            throw new IllegalArgumentException("Cannot toggle an admin token");
        }
        if (token.getId().equals(requesterTokenId)) {
            throw new IllegalArgumentException("Cannot toggle your own token");
        }
        token.setIsActive(active);
        tokenRepository.save(token);
        if (!active) {
            sessionService.revokeSessionsByTokenId(id);
        }
        log.info("Token '{}' (id={}) isActive={}", token.getName(), id, active);
    }

    public List<TokenResponse> listTokens() {
        return tokenRepository.findByRevokedFalse().stream().map(t -> {
            TokenResponse response = new TokenResponse();
            response.setId(t.getId());
            response.setName(t.getName());
            response.setRole(t.getRole());
            response.setEmail(t.getEmail());
            response.setIsActive(t.getIsActive());
            response.setCreatedAt(t.getCreatedAt());
            response.setExpiresAt(t.getExpiresAt());
            return response;
        }).toList();
    }

    @Transactional
    public boolean revokeToken(Long targetId, Long requesterTokenId) {
        Token target = tokenRepository.findById(targetId).orElse(null);
        if (target == null) {
            log.warn("Revoke failed: target token id={} not found", targetId);
            return false;
        }
        if (target.getRevoked()) {
            log.warn("Revoke failed: target token '{}' (id={}) already revoked", target.getName(), targetId);
            return false;
        }

        Token requester = tokenRepository.findById(requesterTokenId).orElse(null);
        if (requester == null || !requester.getIsActive()) {
            return false;
        }

        if (requester.getId().equals(target.getId())) {
            log.warn("Revoke rejected: token '{}' (id={}) attempted to revoke itself", requester.getName(), requester.getId());
            throw new IllegalArgumentException("Cannot revoke your own token");
        }

        if (!"ADMIN".equalsIgnoreCase(requester.getRole())) {
            log.warn("Revoke rejected: non-admin token '{}' (id={}) attempted to revoke '{}' (id={})",
                    requester.getName(), requester.getId(), target.getName(), target.getId());
            throw new IllegalArgumentException("Only admin tokens can revoke other tokens");
        }

        if ("ADMIN".equalsIgnoreCase(target.getRole())) {
            log.warn("Revoke rejected: attempt to revoke admin token '{}' (id={})", target.getName(), targetId);
            throw new IllegalArgumentException("Cannot revoke an admin token");
        }

        target.setIsActive(false);
        target.setRevoked(true);
        tokenRepository.save(target);

        sessionService.revokeSessionsByTokenId(targetId);

        log.info("Revoked {} token '{}' (id={})", target.getRole(), target.getName(), target.getId());
        return true;
    }

    @Transactional
    public TokenResponse refreshToken(Long targetId, Long requesterTokenId) {
        Token target = tokenRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found: " + targetId));
        if (target.getRevoked()) {
            throw new IllegalArgumentException("Cannot refresh a revoked token");
        }
        if (target.getEmail() == null || target.getEmail().isBlank()) {
            throw new IllegalArgumentException("Token has no email set");
        }

        Token requester = tokenRepository.findById(requesterTokenId).orElse(null);
        if (requester == null || !requester.getIsActive()) {
            throw new IllegalArgumentException("Requester token invalid");
        }
        if (!"ADMIN".equalsIgnoreCase(requester.getRole())) {
            throw new IllegalArgumentException("Only admin tokens can refresh other tokens");
        }

        String rawToken = TokenUtil.generateRawToken();
        target.setTokenHash(TokenUtil.hashToken(rawToken));
        tokenRepository.save(target);

        sessionService.revokeSessionsByTokenId(targetId);

        log.info("Refreshed token '{}' (id={})", target.getName(), targetId);

        TokenResponse result = new TokenResponse();
        result.setId(target.getId());
        result.setName(target.getName());
        result.setRole(target.getRole());
        result.setEmail(target.getEmail());
        result.setToken(rawToken);
        return result;
    }

    public boolean hasAnyToken() {
        return tokenRepository.count() > 0;
    }

    public boolean hasEmail(Long tokenId) {
        Token token = tokenRepository.findById(tokenId).orElse(null);
        return token != null && token.getEmail() != null && !token.getEmail().isBlank();
    }

    public String getTokenEmail(Long tokenId) {
        Token token = tokenRepository.findById(tokenId).orElse(null);
        return token != null ? token.getEmail() : null;
    }

    public String getTokenName(Long tokenId) {
        Token token = tokenRepository.findById(tokenId).orElse(null);
        return token != null ? token.getName() : null;
    }
}
