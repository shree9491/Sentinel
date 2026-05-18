package com.gateway.ratelimit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlidingWindowRateLimiter {

    private final StringRedisTemplate redisTemplate;

    public boolean isAllowed(String userId,
                             String endpoint,
                             int maxRequests,
                             int windowSeconds) {

        String key = "rate:" + userId + ":" + endpoint;
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        // Remove old requests outside window
        redisTemplate.opsForZSet()
                .removeRangeByScore(key, 0, windowStart);

        // Count requests in current window
        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count >= maxRequests) {
            log.warn("Rate limit exceeded for user: {} on endpoint: {}",
                    userId, endpoint);
            return false; // BLOCKED
        }

        // Add current request
        String member = now + ":" + java.util.UUID.randomUUID();
        redisTemplate.opsForZSet().add(key, member, now);
        redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);

        log.debug("Request allowed for user: {} count: {}/{}",
                userId, (count == null ? 1 : count + 1), maxRequests);
        return true; // ALLOWED
    }

    public long getRemainingRequests(String userId,
                                     String endpoint,
                                     int maxRequests,
                                     int windowSeconds) {
        String key = "rate:" + userId + ":" + endpoint;
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        redisTemplate.opsForZSet()
                .removeRangeByScore(key, 0, windowStart);
        Long count = redisTemplate.opsForZSet().zCard(key);

        return maxRequests - (count == null ? 0 : count);
    }
}