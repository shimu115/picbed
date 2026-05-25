package com.picbed.controller;

import com.picbed.config.SetupTokenManager;
import com.picbed.dto.Result;
import com.picbed.dto.TokenCreateRequest;
import com.picbed.dto.TokenEmailUpdateRequest;
import com.picbed.dto.TokenRefreshRequest;
import com.picbed.entity.Token;
import com.picbed.service.EmailService;
import com.picbed.service.TokenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class TokenController {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private SetupTokenManager setupTokenManager;
    @Autowired
    private EmailService emailService;

    @GetMapping("/api/public/status")
    public ResponseEntity<Result<Map<String, Object>>> getStatus() {
        boolean initialized = tokenService.hasAnyToken();
        return ResponseEntity.ok(Result.success(Map.of("initialized", initialized)));
    }

    @GetMapping("/api/verify")
    public ResponseEntity<Result<Map<String, Object>>> verifyToken(
            @RequestHeader("X-Auth-Token") String authToken) {
        Token token = tokenService.findByRawToken(authToken).orElse(null);
        if (token == null || !token.getIsActive()) {
            return ResponseEntity.status(401)
                    .body(Result.error("Invalid token", 401));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("valid", true);
        data.put("id", token.getId());
        data.put("name", token.getName());
        data.put("role", token.getRole());
        data.put("email", token.getEmail());
        return ResponseEntity.ok(Result.success(data));
    }

    @PutMapping("/api/account/email")
    public ResponseEntity<Result<Void>> updateOwnEmail(
            @RequestHeader("X-Auth-Token") String authToken,
            @RequestBody TokenEmailUpdateRequest request) {
        Token token = tokenService.findByRawToken(authToken).orElse(null);
        if (token == null || !token.getIsActive()) {
            return ResponseEntity.status(401)
                    .body(Result.error("Invalid token", 401));
        }
        tokenService.updateEmail(token.getId(), request.getEmail());
        return ResponseEntity.ok(Result.success());
    }

    @PostMapping("/api/setup/token")
    public ResponseEntity<Result<Map<String, Object>>> setupToken(
            @RequestHeader(value = "X-Setup-Token", required = false) String masterToken,
            @Valid @RequestBody TokenCreateRequest request) {

        if (masterToken == null || masterToken.isBlank()) {
            log.warn("Setup failed: missing master token header");
            return ResponseEntity.status(401)
                    .body(Result.error("Missing X-Setup-Token header", 401));
        }

        if (!setupTokenManager.validate(masterToken)) {
            log.warn("Setup failed: invalid master token provided");
            return ResponseEntity.status(401)
                    .body(Result.error("Invalid setup token", 401));
        }

        if (tokenService.hasAnyToken()) {
            log.warn("Setup rejected: tokens already exist");
            return ResponseEntity.badRequest()
                    .body(Result.error("Tokens already exist, use admin API to create more", 400));
        }

        log.info("Creating initial admin token for '{}'", request.getName());
        return ResponseEntity.ok(Result.success(tokenService.createToken(request.getName(), "ADMIN", request.getEmail())));
    }

    @GetMapping("/api/admin/tokens")
    public ResponseEntity<Result<List<Map<String, Object>>>> listTokens(
            @RequestHeader("X-Auth-Token") String authToken) {
        Token requester = tokenService.findByRawToken(authToken).orElse(null);
        if (requester == null || !"ADMIN".equalsIgnoreCase(requester.getRole())) {
            log.warn("Token list rejected: requester has no admin role");
            return ResponseEntity.status(403)
                    .body(Result.error("Admin role required", 403));
        }
        return ResponseEntity.ok(Result.success(tokenService.listTokens()));
    }

    @PostMapping("/api/admin/tokens")
    public ResponseEntity<Result<Map<String, Object>>> createToken(
            @RequestHeader("X-Auth-Token") String authToken,
            @Valid @RequestBody TokenCreateRequest request) {
        Token requester = tokenService.findByRawToken(authToken).orElse(null);
        if (requester == null || !"ADMIN".equalsIgnoreCase(requester.getRole())) {
            log.warn("Token creation rejected: requester has no admin role");
            return ResponseEntity.status(403)
                    .body(Result.error("Admin role required", 403));
        }
        String role = request.getRole() != null ? request.getRole() : "USER";
        return ResponseEntity.ok(Result.success(tokenService.createToken(request.getName(), role, request.getEmail())));
    }

    @PutMapping("/api/admin/tokens/{id}/email")
    public ResponseEntity<Result<Void>> updateTokenEmail(
            @RequestHeader("X-Auth-Token") String authToken,
            @PathVariable Long id,
            @RequestBody TokenEmailUpdateRequest request) {
        Token requester = tokenService.findByRawToken(authToken).orElse(null);
        if (requester == null || !"ADMIN".equalsIgnoreCase(requester.getRole())) {
            return ResponseEntity.status(403)
                    .body(Result.error("Admin role required", 403));
        }
        try {
            tokenService.updateEmail(id, request.getEmail());
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @DeleteMapping("/api/admin/tokens/{id}")
    public ResponseEntity<Result<Void>> revokeToken(
            @RequestHeader("X-Auth-Token") String authToken,
            @PathVariable Long id) {
        try {
            if (tokenService.revokeToken(id, authToken)) {
                return ResponseEntity.ok(Result.success());
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/api/account/refresh")
    public ResponseEntity<Result<Map<String, Object>>> refreshOwnToken(
            @RequestHeader("X-Auth-Token") String authToken,
            @RequestBody TokenRefreshRequest request) {
        Token token = tokenService.findByRawToken(authToken).orElse(null);
        if (token == null || !token.getIsActive()) {
            return ResponseEntity.status(401)
                    .body(Result.error("Invalid token", 401));
        }
        try {
            String newRawToken = tokenService.refreshOwnToken(token.getId(), request.isSendEmail());
            if (request.isSendEmail()) {
                emailService.sendTokenRefresh(token.getEmail(), token.getName(), newRawToken);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("token", newRawToken);
            data.put("id", token.getId());
            data.put("name", token.getName());
            return ResponseEntity.ok(Result.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/api/admin/tokens/{id}/refresh")
    public ResponseEntity<Result<Map<String, Object>>> adminRefreshToken(
            @RequestHeader("X-Auth-Token") String authToken,
            @PathVariable Long id) {
        Token requester = tokenService.findByRawToken(authToken).orElse(null);
        if (requester == null || !"ADMIN".equalsIgnoreCase(requester.getRole())) {
            return ResponseEntity.status(403)
                    .body(Result.error("Admin role required", 403));
        }
        try {
            String newRawToken = tokenService.adminRefreshToken(id);
            String email = tokenService.getTokenEmail(id);
            String targetName = tokenService.listTokens().stream()
                    .filter(m -> m.get("id").equals(id))
                    .findFirst().map(m -> (String) m.get("name")).orElse("Unknown");
            emailService.sendTokenRefresh(email, targetName, newRawToken);
            Map<String, Object> data = new HashMap<>();
            data.put("token", newRawToken);
            data.put("id", id);
            return ResponseEntity.ok(Result.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/api/admin/tokens/{id}/warn")
    public ResponseEntity<Result<Void>> warnToken(
            @RequestHeader("X-Auth-Token") String authToken,
            @PathVariable Long id) {
        Token requester = tokenService.findByRawToken(authToken).orElse(null);
        if (requester == null || !"ADMIN".equalsIgnoreCase(requester.getRole())) {
            return ResponseEntity.status(403)
                    .body(Result.error("Admin role required", 403));
        }
        String email = tokenService.getTokenEmail(id);
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Result.error("Token has no email set, cannot send warning", 400));
        }
        String targetName = tokenService.listTokens().stream()
                .filter(m -> m.get("id").equals(id))
                .findFirst().map(m -> (String) m.get("name")).orElse("Unknown");
        emailService.sendTokenCompromisedWarning(email, targetName);
        return ResponseEntity.ok(Result.success());
    }
}
