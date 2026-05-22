package com.picbed.interceptor;

import com.picbed.exception.UnauthorizedException;
import com.picbed.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "X-Auth-Token";

    private final TokenService tokenService;

    public TokenInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(AUTH_HEADER);
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing X-Auth-Token header");
        }

        if (!tokenService.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        return true;
    }
}
