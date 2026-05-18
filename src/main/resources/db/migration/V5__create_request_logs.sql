CREATE TABLE request_logs (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id UUID,
                              ip_address VARCHAR(50),
                              endpoint VARCHAR(200),
                              method VARCHAR(10),
                              status_code INT,
                              response_time_ms BIGINT,
                              timestamp TIMESTAMP DEFAULT NOW()
);