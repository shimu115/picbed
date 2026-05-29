package com.picbed.interceptor;

import com.picbed.entity.Session;
import com.picbed.exception.UnauthorizedException;
import com.picbed.service.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Slf4j
@Component
public class SessionInterceptor implements HandlerInterceptor {

    private static final String SESSION_COOKIE = "SESSION_ID";

    private final SessionService sessionService;

    public SessionInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String sessionId = extractSessionId(request.getCookies());
        if (sessionId == null) {
            log.warn("Missing SESSION_ID cookie from {} to {}", request.getRemoteAddr(), request.getRequestURI());
            throw new UnauthorizedException("Missing session cookie");
        }

        Optional<Session> opt = sessionService.validateSession(sessionId);
        if (opt.isEmpty()) {
            log.warn("Invalid/expired session {} from {} to {}", sessionId, request.getRemoteAddr(), request.getRequestURI());
            throw new UnauthorizedException("Invalid or expired session");
        }

        Session session = opt.get();
        request.setAttribute("tokenId", session.getTokenId());
        request.setAttribute("tokenRole", session.getRole());
        request.setAttribute("sessionId", session.getSessionId());

        return true;
    }

    private String extractSessionId(Cookie[] cookies) {
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (SESSION_COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
