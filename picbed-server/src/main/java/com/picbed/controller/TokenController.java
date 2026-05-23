package com.picbed.controller;

import com.picbed.config.SetupTokenManager;
import com.picbed.dto.Result;
import com.picbed.dto.TokenCreateRequest;
import com.picbed.service.TokenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
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

    @PostMapping("/api/setup/token")
    public ResponseEntity<Result<Map<String, Object>>> setupToken(
            @RequestHeader(value = "X-Setup-Token", required = false) String masterToken,
            @Valid @RequestBody TokenCreateRequest request) {

        if (masterToken == null || masterToken.isBlank()) {
            return ResponseEntity.status(401)
                    .body(Result.error("Missing X-Setup-Token header", 401));
        }

        if (!setupTokenManager.validate(masterToken)) {
            return ResponseEntity.status(401)
                    .body(Result.error("Invalid setup token", 401));
        }

        if (tokenService.hasAnyToken()) {
            return ResponseEntity.badRequest()
                    .body(Result.error("Tokens already exist, use admin API to create more", 400));
        }

        log.info("Creating token for {}", request.getName());
        return ResponseEntity.ok(Result.success(tokenService.createToken(request.getName())));
    }

    @GetMapping("/api/admin/tokens")
    public ResponseEntity<Result<List<Map<String, Object>>>> listTokens() {
        return ResponseEntity.ok(Result.success(tokenService.listTokens()));
    }

    @PostMapping("/api/admin/tokens")
    public ResponseEntity<Result<Map<String, Object>>> createToken(
            @Valid @RequestBody TokenCreateRequest request) {
        return ResponseEntity.ok(Result.success(tokenService.createToken(request.getName())));
    }

    @DeleteMapping("/api/admin/tokens/{id}")
    public ResponseEntity<Result<Void>> revokeToken(@PathVariable Long id) {
        if (tokenService.revokeToken(id)) {
            return ResponseEntity.ok(Result.success());
        }
        return ResponseEntity.notFound().build();
    }
}
