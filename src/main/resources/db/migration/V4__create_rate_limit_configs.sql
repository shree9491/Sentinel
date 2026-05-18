CREATE TABLE rate_limit_configs (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                                    endpoint_pattern VARCHAR(200) DEFAULT '*',
                                    max_requests INT DEFAULT 100,
                                    window_seconds INT DEFAULT 60
);