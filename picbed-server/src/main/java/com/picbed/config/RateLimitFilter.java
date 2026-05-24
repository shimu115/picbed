package com.picbed.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitFilter implements Filter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String key = ip + ":" + getPathCategory(path);

        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(path));
        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            log.warn("Rate limit exceeded for {} to {}", ip, path);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":429,\"message\":\"Too many requests\"}");
        }
    }

    private String getPathCategory(String path) {
        if (path.contains("/upload/")) return "upload";
        if (path.contains("/admin/")) return "admin";
        return "public";
    }

    private Bucket createBucket(String path) {
        Bandwidth limit;
        if (path.contains("/upload/")) {
            limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        } else if (path.contains("/admin/")) {
            limit = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
        } else {
            limit = Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));
        }
        return Bucket.builder().addLimit(limit).build();
    }
}
