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

    @Transactional
    public Map<String, Object> createToken(String name, String role) {
        String rawToken = TokenUtil.generateRawToken();
        String tokenHash = TokenUtil.hashToken(rawToken);

        String resolvedRole = (role != null && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("USER")))
                ? role.toUpperCase() : "USER";

        Token token = new Token();
        token.setName(name);
        token.setTokenHash(tokenHash);
        token.setRole(resolvedRole);
        token.setIsActive(true);
        tokenRepository.save(token);

        log.info("Created {} token '{}' (id={})", resolvedRole, name, token.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("id", token.getId());
        result.put("name", token.getName());
        result.put("role", token.getRole());
        result.put("token", rawToken);
        result.put("createdAt", token.getCreatedAt());
        return result;
    }

    public boolean validateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        String hash = TokenUtil.hashToken(rawToken);
        Optional<Token> opt = tokenRepository.findByTokenHashAndIsActiveTrue(hash);
        if (opt.isEmpty()) {
            return false;
        }
        return !TokenUtil.isExpired(opt.get().getExpiresAt());
    }

    public Optional<Token> findByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return tokenRepository.findByTokenHash(TokenUtil.hashToken(rawToken));
    }

    public List<Map<String, Object>> listTokens() {
        return tokenRepository.findByIsActiveTrue().stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("role", t.getRole());
            m.put("isActive", t.getIsActive());
            m.put("createdAt", t.getCreatedAt());
            m.put("expiresAt", t.getExpiresAt());
            return m;
        }).toList();
    }

    @Transactional
    public boolean revokeToken(Long id, String requesterRawToken) {
        Token target = tokenRepository.findById(id).orElse(null);
        if (target == null) {
            log.warn("Revoke failed: target token id={} not found", id);
            return false;
        }
        if (!target.getIsActive()) {
            log.warn("Revoke failed: target token '{}' (id={}) already revoked", target.getName(), id);
            return false;
        }

        Token requester = findByRawToken(requesterRawToken).orElse(null);
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
            long adminCount = tokenRepository.countByIsActiveTrueAndRole("ADMIN");
            if (adminCount <= 1) {
                log.warn("Revoke rejected: attempt to revoke last admin token '{}' (id={})", target.getName(), id);
                throw new IllegalArgumentException("Cannot revoke the last admin token");
            }
        }

        target.setIsActive(false);
        tokenRepository.save(target);
        log.info("Revoked {} token '{}' (id={})", target.getRole(), target.getName(), target.getId());
        return true;
    }

    public boolean hasAnyToken() {
        return tokenRepository.count() > 0;
    }
}
