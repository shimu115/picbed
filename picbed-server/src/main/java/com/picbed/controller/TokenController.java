package com.picbed.controller;

import com.picbed.config.SetupTokenManager;
import com.picbed.dto.ApiResponse;
import com.picbed.dto.TokenCreateRequest;
import com.picbed.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TokenController {

    private final TokenService tokenService;
    private final SetupTokenManager setupTokenManager;

    public TokenController(TokenService tokenService, SetupTokenManager setupTokenManager) {
        this.tokenService = tokenService;
        this.setupTokenManager = setupTokenManager;
    }

    @PostMapping("/api/setup/token")
    public ResponseEntity<ApiResponse<?>> setupToken(
            @RequestHeader(value = "X-Setup-Token", required = false) String masterToken,
            @Valid @RequestBody TokenCreateRequest request) {

        if (masterToken == null || masterToken.isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Missing X-Setup-Token header"));
        }

        if (!setupTokenManager.validate(masterToken)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Invalid setup token"));
        }

        if (tokenService.hasAnyToken()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Tokens already exist, use admin API to create more"));
        }

        return ResponseEntity.ok(ApiResponse.ok(tokenService.createToken(request.getName())));
    }

    @GetMapping("/api/admin/tokens")
    public ResponseEntity<ApiResponse<?>> listTokens() {
        return ResponseEntity.ok(ApiResponse.ok(tokenService.listTokens()));
    }

    @PostMapping("/api/admin/tokens")
    public ResponseEntity<ApiResponse<?>> createToken(@Valid @RequestBody TokenCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(tokenService.createToken(request.getName())));
    }

    @DeleteMapping("/api/admin/tokens/{id}")
    public ResponseEntity<ApiResponse<?>> revokeToken(@PathVariable Long id) {
        if (tokenService.revokeToken(id)) {
            return ResponseEntity.ok(ApiResponse.ok());
        }
        return ResponseEntity.notFound().build();
    }
}
