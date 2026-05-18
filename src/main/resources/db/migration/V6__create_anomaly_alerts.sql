CREATE TABLE anomaly_alerts (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                ip_address VARCHAR(50),
                                alert_type VARCHAR(50),
                                severity VARCHAR(20),
                                anomaly_score DOUBLE PRECISION,
                                description TEXT,
                                detected_at TIMESTAMP DEFAULT NOW(),
                                resolved BOOLEAN DEFAULT FALSE
);