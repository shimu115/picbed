package com.picbed.service;

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
    public Map<String, Object> createToken(String name, String role, String email) {
        if (tokenRepository.existsByName(name)) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("id", token.getId());
        result.put("name", token.getName());
        result.put("role", token.getRole());
        result.put("email", token.getEmail());
        result.put("token", rawToken);
        result.put("createdAt", token.getCreatedAt());
        return result;
    }

    public boolean validateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        String hash = TokenUtil.hashToken(rawToken);
        return tokenRepository.findByTokenHashAndIsActiveTrue(hash).isPresent();
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
        if (normalized != null && tokenRepository.existsByEmailAndIsActiveTrue(normalized)
                && !normalized.equals(token.getEmail())) {
            throw new IllegalArgumentException("邮箱已被其他用户使用，请换一个邮箱");
        }
        token.setEmail(normalized);
        tokenRepository.save(token);
        log.info("Updated email for token '{}' (id={})", token.getName(), token.getId());
    }

    public List<Token> findAllActive() {
        return tokenRepository.findByIsActiveTrue();
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

    public List<Map<String, Object>> listTokens() {
        return tokenRepository.findByIsActiveTrue().stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("role", t.getRole());
            m.put("email", t.getEmail());
            m.put("isActive", t.getIsActive());
            m.put("createdAt", t.getCreatedAt());
            m.put("expiresAt", t.getExpiresAt());
            return m;
        }).toList();
    }

    @Transactional
    public boolean revokeToken(Long targetId, Long requesterTokenId) {
        Token target = tokenRepository.findById(targetId).orElse(null);
        if (target == null) {
            log.warn("Revoke failed: target token id={} not found", targetId);
            return false;
        }
        if (!target.getIsActive()) {
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
        tokenRepository.save(target);

        sessionService.revokeSessionsByTokenId(targetId);

        log.info("Revoked {} token '{}' (id={})", target.getRole(), target.getName(), target.getId());
        return true;
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
}
