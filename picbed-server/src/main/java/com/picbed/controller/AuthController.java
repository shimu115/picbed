package com.picbed.controller;

import com.picbed.dto.Result;
import com.picbed.dto.SessionResponse;
import com.picbed.entity.Session;
import com.picbed.entity.Token;
import com.picbed.service.SessionService;
import com.picbed.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
public class AuthController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SessionService sessionService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<Result<SessionResponse>> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Result.error("Token is required", 400));
        }

        String status = tokenService.validateTokenStatus(token);
        switch (status) {
            case "revoked":
                return ResponseEntity.status(401)
                        .body(Result.error("Token has been revoked, contact admin for a new one", 401));
            case "disabled":
                return ResponseEntity.status(403)
                        .body(Result.error("Token disabled, contact admin", 403));
            case "invalid":
                return ResponseEntity.status(401)
                        .body(Result.error("Invalid token", 401));
        }

        Token tokenEntity = tokenService.findByRawToken(token).orElse(null);
        if (tokenEntity == null) {
            return ResponseEntity.status(401)
                    .body(Result.error("Invalid token", 401));
        }

        Session session = sessionService.createSession(tokenEntity.getId(), tokenEntity.getRole());

        boolean secure = request.isSecure();
        Cookie cookie = new Cookie("SESSION_ID", session.getSessionId());
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(SessionService.COOKIE_MAX_AGE_SECONDS);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        SessionResponse data = new SessionResponse();
        data.setId(tokenEntity.getId());
        data.setName(tokenEntity.getName());
        data.setRole(tokenEntity.getRole());
        data.setEmail(tokenEntity.getEmail());
        log.info("User '{}' (tokenId={}) logged in, session={}", tokenEntity.getName(), tokenEntity.getId(), session.getSessionId());
        return ResponseEntity.ok(Result.success(data));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Result<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("SESSION_ID".equals(c.getName())) {
                    sessionService.deleteSession(c.getValue());
                }
            }
        }

        Cookie cookie = new Cookie("SESSION_ID", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/api/auth/session")
    public ResponseEntity<Result<SessionResponse>> getSession(
            HttpServletRequest request) {
        Long tokenId = (Long) request.getAttribute("tokenId");
        String sessionId = (String) request.getAttribute("sessionId");

        Optional<Session> sessionOpt = sessionService.validateSession(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Result.error("Session expired", 401));
        }

        String name = tokenService.getTokenName(tokenId);

        String email = tokenService.getTokenEmail(tokenId);

        SessionResponse data = new SessionResponse();
        data.setId(tokenId);
        data.setName(name);
        data.setRole(sessionOpt.get().getRole());
        data.setEmail(email);
        data.setValid(true);
        return ResponseEntity.ok(Result.success(data));
    }
}
