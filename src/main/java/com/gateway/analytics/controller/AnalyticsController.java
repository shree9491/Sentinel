package com.gateway.analytics.controller;

import com.gateway.logging.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final RequestLogRepository logRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        Long requestsLastHour = logRepository.countRequestsSince(oneHourAgo);
        Long requestsLastDay = logRepository.countRequestsSince(oneDayAgo);
        Double avgResponseTime = logRepository.avgResponseTimeSince(oneHourAgo);
        List<Object[]> topEndpoints = logRepository.findTopEndpoints();

        List<Map<String, Object>> endpointStats = new ArrayList<>();
        for (Object[] row : topEndpoints) {
            endpointStats.add(Map.of(
                    "endpoint", row[0],
                    "hits", row[1]
            ));
        }

        return ResponseEntity.ok(Map.of(
                "requestsLastHour", requestsLastHour,
                "requestsLastDay", requestsLastDay,
                "avgResponseTimeMs", avgResponseTime != null ? avgResponseTime : 0,
                "topEndpoints", endpointStats,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getRecentLogs() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return ResponseEntity.ok(
                logRepository.findByTimestampAfter(oneHourAgo)
        );
    }
}