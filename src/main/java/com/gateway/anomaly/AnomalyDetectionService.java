package com.gateway.anomaly;

import com.gateway.logging.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private final RequestLogRepository logRepository;
    private final AnomalyAlertRepository alertRepository;

    @Scheduled(fixedDelay = 60000)
    public void detectAnomalies() {
        try {
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            var recentLogs = logRepository.findByTimestampAfter(oneMinuteAgo);

            if (recentLogs.isEmpty()) return;

            Map<String, List<Object>> byIp = recentLogs.stream()
                    .collect(Collectors.groupingBy(
                            l -> l.getIpAddress() != null ? l.getIpAddress() : "unknown",
                            Collectors.mapping(l -> (Object) l, Collectors.toList())
                    ));

            byIp.forEach((ip, logs) -> {
                double rpm = logs.size();
                long uniqueEndpoints = recentLogs.stream()
                        .filter(l -> ip.equals(l.getIpAddress()))
                        .map(l -> l.getEndpoint())
                        .distinct().count();
                double errorRate = recentLogs.stream()
                        .filter(l -> ip.equals(l.getIpAddress()))
                        .filter(l -> l.getStatusCode() != null && l.getStatusCode() >= 400)
                        .count() / rpm;
                double avgResponseTime = recentLogs.stream()
                        .filter(l -> ip.equals(l.getIpAddress()))
                        .mapToLong(l -> l.getResponseTimeMs() != null ? l.getResponseTimeMs() : 0)
                        .average().orElse(0);
                int hour = LocalDateTime.now().getHour();

                Map<String, Object> features = Map.of(
                        "requests_per_minute", rpm,
                        "unique_endpoints", (int) uniqueEndpoints,
                        "error_rate", errorRate,
                        "avg_response_time", avgResponseTime,
                        "hour_of_day", hour
                );

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    Map result = restTemplate.postForObject(
                            "http://localhost:8000/predict",
                            features, Map.class
                    );

                    if (result != null && Boolean.TRUE.equals(result.get("is_anomaly"))) {
                        AnomalyAlert alert = AnomalyAlert.builder()
                                .ipAddress(ip)
                                .alertType("TRAFFIC_ANOMALY")
                                .severity((String) result.get("severity"))
                                .anomalyScore(((Number) result.get("anomaly_score")).doubleValue())
                                .description("Abnormal traffic detected. " + result.get("message"))
                                .detectedAt(LocalDateTime.now())
                                .resolved(false)
                                .build();
                        alertRepository.save(alert);
                        log.warn("ANOMALY DETECTED for IP: {} | Severity: {} | Score: {}",
                                ip, result.get("severity"), result.get("anomaly_score"));
                    }
                } catch (Exception e) {
                    log.error("ML service call failed: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Anomaly detection error: {}", e.getMessage());
        }
    }
}