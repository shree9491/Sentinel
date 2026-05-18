CREATE TABLE api_keys (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                          key_hash VARCHAR(255) UNIQUE NOT NULL,
                          name VARCHAR(100),
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT NOW()
);