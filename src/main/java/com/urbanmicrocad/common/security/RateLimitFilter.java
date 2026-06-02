package com.urbanmicrocad.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanmicrocad.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate-limiting filter for authentication endpoints.
 * <p>
 * Applies IP-based sliding window rate limiting to {@code /api/auth/login}
 * and {@code /api/auth/register} to prevent brute-force attacks and abuse.
 * Uses in-memory token buckets — no external dependencies required.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimiter loginLimiter;
    private final RateLimiter registerLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
        this.loginLimiter = new RateLimiter(
            properties.getLoginMaxRequests(),
            properties.getLoginWindowSeconds()
        );
        this.registerLimiter = new RateLimiter(
            properties.getRegisterMaxRequests(),
            properties.getRegisterWindowSeconds()
        );
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);

        if ("/api/auth/login".equals(path)) {
            if (!loginLimiter.tryAcquire(clientIp)) {
                log.warn("Login rate limit exceeded for IP: {}", clientIp);
                writeRateLimitResponse(response);
                return;
            }
        } else if ("/api/auth/register".equals(path)) {
            if (!registerLimiter.tryAcquire(clientIp)) {
                log.warn("Register rate limit exceeded for IP: {}", clientIp);
                writeRateLimitResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(429, "请求过于频繁，请稍后再试"));
    }

    /**
     * Periodic cleanup of stale rate-limit buckets to prevent memory leaks.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanupStaleBuckets() {
        loginLimiter.cleanup();
        registerLimiter.cleanup();
    }
}
