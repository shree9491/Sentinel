package com.gateway.ratelimit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.auth.service.TokenService;
import com.gateway.ratelimit.service.SlidingWindowRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final SlidingWindowRateLimiter rateLimiter;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Value("${rate-limit.default-max-requests}")
    private int maxRequests;

    @Value("${rate-limit.default-window-seconds}")
    private int windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Skip rate limiting for auth endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/auth/") || path.startsWith("/actuator/")) {
            chain.doFilter(request, response);
            return;
        }

        // Extract user identity
        String userId = extractUserId(request);

        // Check rate limit
        boolean allowed = rateLimiter.isAllowed(
                userId, path, maxRequests, windowSeconds);

        long remaining = rateLimiter.getRemainingRequests(
                userId, path, maxRequests, windowSeconds);

        // Add rate limit headers
        response.addHeader("X-RateLimit-Limit",
                String.valueOf(maxRequests));
        response.addHeader("X-RateLimit-Remaining",
                String.valueOf(remaining));
        response.addHeader("X-RateLimit-Window",
                windowSeconds + "s");

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> error = Map.of(
                    "error", "Rate limit exceeded",
                    "message", "Too many requests. Try again later.",
                    "retryAfter", windowSeconds,
                    "timestamp", LocalDateTime.now().toString()
            );

            response.getWriter()
                    .write(objectMapper.writeValueAsString(error));
            return;
        }

        chain.doFilter(request, response);
    }

    private String extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (tokenService.isValid(token)) {
                return tokenService.extractUsername(token);
            }
        }
        // Fall back to IP address
        return request.getRemoteAddr();
    }
}