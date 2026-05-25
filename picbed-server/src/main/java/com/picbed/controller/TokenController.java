package com.picbed.controller;

import com.picbed.config.SetupTokenManager;
import com.picbed.dto.Result;
import com.picbed.dto.TokenCreateRequest;
import com.picbed.entity.Token;
import com.picbed.service.TokenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class TokenController {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private SetupTokenManager setupTokenManager;

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
        return ResponseEntity.ok(Result.success(Map.of(
                "valid", true,
                "id", token.getId(),
                "name", token.getName(),
                "role", token.getRole()
        )));
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
}
