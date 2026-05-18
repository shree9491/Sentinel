package com.gateway.logging.filter;

import com.gateway.logging.model.RequestLog;
import com.gateway.logging.repository.RequestLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository logRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        try {
            chain.doFilter(request, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - start;
            saveLog(request, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void saveLog(HttpServletRequest request,
                         ContentCachingResponseWrapper response,
                         long duration) {
        try {
            RequestLog requestLog = RequestLog.builder()
                    .ipAddress(request.getRemoteAddr())
                    .endpoint(request.getRequestURI())
                    .method(request.getMethod())
                    .statusCode(response.getStatus())
                    .responseTimeMs(duration)
                    .timestamp(LocalDateTime.now())
                    .build();

            logRepository.save(requestLog);

            log.info("REQUEST: {} {} → {} ({}ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);

        } catch (Exception e) {
            log.error("Failed to save request log: {}", e.getMessage());
        }
    }
}