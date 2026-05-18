package com.gateway.anomaly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnomalyAlertRepository extends JpaRepository<AnomalyAlert, UUID> {
    List<AnomalyAlert> findByResolvedFalseOrderByDetectedAtDesc();
    List<AnomalyAlert> findBySeverityOrderByDetectedAtDesc(String severity);
}