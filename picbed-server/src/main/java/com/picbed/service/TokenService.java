package com.picbed.service;

import com.picbed.entity.Token;
import com.picbed.repository.TokenRepository;
import com.picbed.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public Map<String, Object> createToken(String name) {
        String rawToken = TokenUtil.generateRawToken();
        String tokenHash = TokenUtil.hashToken(rawToken);

        Token token = new Token();
        token.setName(name);
        token.setTokenHash(tokenHash);
        token.setIsActive(true);
        tokenRepository.save(token);

        Map<String, Object> result = new HashMap<>();
        result.put("id", token.getId());
        result.put("name", token.getName());
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
        Token token = opt.get();
        return !TokenUtil.isExpired(token.getExpiresAt());
    }

    public List<Map<String, Object>> listTokens() {
        return tokenRepository.findByIsActiveTrue().stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("isActive", t.getIsActive());
            m.put("createdAt", t.getCreatedAt());
            m.put("expiresAt", t.getExpiresAt());
            return m;
        }).toList();
    }

    @Transactional
    public boolean revokeToken(Long id) {
        return tokenRepository.findById(id).map(t -> {
            t.setIsActive(false);
            tokenRepository.save(t);
            return true;
        }).orElse(false);
    }

    public boolean hasAnyToken() {
        return tokenRepository.count() > 0;
    }
}
