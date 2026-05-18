package com.gateway.anomaly;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/anomaly")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyAlertRepository alertRepository;

    @GetMapping("/alerts")
    public ResponseEntity<List<AnomalyAlert>> getAlerts() {
        return ResponseEntity.ok(
                alertRepository.findByResolvedFalseOrderByDetectedAtDesc()
        );
    }

    @GetMapping("/alerts/high")
    public ResponseEntity<List<AnomalyAlert>> getHighAlerts() {
        return ResponseEntity.ok(
                alertRepository.findBySeverityOrderByDetectedAtDesc("HIGH")
        );
    }

    @PutMapping("/alerts/{id}/resolve")
    public ResponseEntity<String> resolve(@PathVariable String id) {
        alertRepository.findById(java.util.UUID.fromString(id)).ifPresent(alert -> {
            alert.setResolved(true);
            alertRepository.save(alert);
        });
        return ResponseEntity.ok("Alert resolved");
    }
}