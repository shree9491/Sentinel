package com.gateway.anomaly;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "anomaly_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "alert_type")
    private String alertType;

    @Column(name = "severity")
    private String severity;

    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(name = "description")
    private String description;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "resolved")
    private Boolean resolved;
}