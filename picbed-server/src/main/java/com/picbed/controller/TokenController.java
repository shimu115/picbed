package com.picbed.controller;

import com.picbed.config.SetupTokenManager;
import com.picbed.dto.CodeRequest;
import com.picbed.dto.Result;
import com.picbed.dto.SendCodeRequest;
import com.picbed.dto.TokenCreateRequest;
import com.picbed.dto.TokenEmailUpdateRequest;
import com.picbed.dto.VerifyCodeRequest;
import com.picbed.entity.Token;
import com.picbed.repository.EmailDomainRepository;
import com.picbed.service.EmailService;
import com.picbed.service.EmailVerificationService;
import com.picbed.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
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
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private EmailDomainRepository emailDomainRepository;

    @GetMapping("/api/public/status")
    public ResponseEntity<Result<Map<String, Object>>> getStatus() {
        boolean initialized = tokenService.hasAnyToken();
        return ResponseEntity.ok(Result.success(Map.of("initialized", initialized)));
    }

    @GetMapping("/api/public/email-domains")
    public ResponseEntity<Result<List<String>>> getEmailDomains() {
        List<String> domains = emailDomainRepository.findAll().stream()
                .map(d -> d.getDomain())
                .toList();
        return ResponseEntity.ok(Result.success(domains));
    }

    @PutMapping("/api/account/email")
    public ResponseEntity<Result<Void>> updateOwnEmail(
            HttpServletRequest request,
            @RequestBody TokenEmailUpdateRequest body) {
        Long tokenId = (Long) request.getAttribute("tokenId");
        tokenService.updateEmail(tokenId, body.getEmail());
        return ResponseEntity.ok(Result.success());
    }

    @PostMapping("/api/account/email/send-code")
    public ResponseEntity<Result<Void>> sendVerificationCode(
            HttpServletRequest request,
            @Valid @RequestBody SendCodeRequest req) {
        Long tokenId = (Long) request.getAttribute("tokenId");
        try {
            emailVerificationService.sendCode(tokenId, req.getEmail());
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(429).body(Result.error(e.getMessage(), 429));
        }
    }

    @PostMapping("/api/account/email/verify")
    public ResponseEntity<Result<Void>> verifyEmailCode(
            HttpServletRequest request,
            @Valid @RequestBody VerifyCodeRequest req) {
        Long tokenId = (Long) request.getAttribute("tokenId");
        try {
            emailVerificationService.verifyCode(tokenId, req.getEmail(), req.getCode());
            tokenService.updateEmail(tokenId, req.getEmail());
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
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

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Result.error("Email is required for setup", 400));
        }

        log.info("Creating initial admin token for '{}'", request.getName());
        Map<String, Object> result = tokenService.createToken(request.getName(), "ADMIN", request.getEmail());
        String rawToken = (String) result.get("token");

        emailService.sendTokenCreated(request.getEmail(), request.getName(), rawToken);

        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/api/admin/tokens/listTokens")
    public ResponseEntity<Result<List<Map<String, Object>>>> listTokens(
            HttpServletRequest request) {
        return ResponseEntity.ok(Result.success(tokenService.listTokens()));
    }

    @PostMapping("/api/admin/tokens/createTokens")
    public ResponseEntity<Result<Map<String, Object>>> createToken(
            HttpServletRequest request,
            @Valid @RequestBody TokenCreateRequest req) {
        String role = req.getRole() != null ? req.getRole() : "USER";
        return ResponseEntity.ok(Result.success(tokenService.createToken(req.getName(), role, req.getEmail())));
    }

    @PutMapping("/api/admin/tokens/{id}/email")
    public ResponseEntity<Result<Void>> updateTokenEmail(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody TokenEmailUpdateRequest req) {
        try {
            tokenService.updateEmail(id, req.getEmail());
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/api/admin/tokens/{id}/email/send-code")
    public ResponseEntity<Result<Void>> adminSendVerificationCode(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody(required = false) SendCodeRequest req) {
        String email;
        if (req != null && req.getEmail() != null && !req.getEmail().isBlank()) {
            email = req.getEmail().trim().toLowerCase();
        } else {
            email = tokenService.getTokenEmail(id);
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Result.error("Token has no email set", 400));
        }
        try {
            emailVerificationService.sendCode(id, email);
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(429).body(Result.error(e.getMessage(), 429));
        }
    }

    @PostMapping("/api/admin/tokens/{id}/email/verify-code")
    public ResponseEntity<Result<Void>> adminVerifyEmailCode(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody CodeRequest req) {
        String email = tokenService.getTokenEmail(id);
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Result.error("Token has no email set", 400));
        }
        try {
            emailVerificationService.verifyCode(id, email, req.getCode());
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @DeleteMapping("/api/admin/tokens/{id}")
    public ResponseEntity<Result<Void>> revokeToken(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long requesterTokenId = (Long) request.getAttribute("tokenId");
        try {
            if (tokenService.revokeToken(id, requesterTokenId)) {
                return ResponseEntity.ok(Result.success());
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @PutMapping("/api/admin/tokens/{id}/active")
    public ResponseEntity<Result<Void>> toggleTokenActive(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Long requesterTokenId = (Long) request.getAttribute("tokenId");
        boolean active = body.getOrDefault("active", true);
        try {
            tokenService.toggleTokenActive(id, active, requesterTokenId);
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/api/admin/tokens/{id}/refresh")
    public ResponseEntity<Result<Map<String, Object>>> refreshToken(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long requesterTokenId = (Long) request.getAttribute("tokenId");
        try {
            Map<String, Object> result = tokenService.refreshToken(id, requesterTokenId);
            String targetEmail = (String) result.get("email");
            String targetName = (String) result.get("name");
            String rawToken = (String) result.get("token");
            emailService.sendTokenRefreshed(targetEmail, targetName, rawToken);
            return ResponseEntity.ok(Result.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/api/admin/tokens/{id}/warn")
    public ResponseEntity<Result<Void>> warnToken(
            HttpServletRequest request,
            @PathVariable Long id) {
        String email = tokenService.getTokenEmail(id);
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Result.error("Token has no email set, cannot send warning", 400));
        }
        String targetName = tokenService.getTokenName(id) != null ? tokenService.getTokenName(id) : "Unknown";
        emailService.sendTokenCompromisedWarning(email, targetName);
        return ResponseEntity.ok(Result.success());
    }
}
