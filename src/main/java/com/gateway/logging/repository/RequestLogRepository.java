package com.gateway.logging.repository;

import com.gateway.logging.model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, UUID> {

    List<RequestLog> findByTimestampAfter(LocalDateTime timestamp);

    List<RequestLog> findByIpAddress(String ipAddress);

    @Query("SELECT r.endpoint, COUNT(r) as hits FROM RequestLog r " +
            "GROUP BY r.endpoint ORDER BY hits DESC")
    List<Object[]> findTopEndpoints();

    @Query("SELECT COUNT(r) FROM RequestLog r " +
            "WHERE r.timestamp > :since")
    Long countRequestsSince(LocalDateTime since);

    @Query("SELECT AVG(r.responseTimeMs) FROM RequestLog r " +
            "WHERE r.timestamp > :since")
    Double avgResponseTimeSince(LocalDateTime since);
}