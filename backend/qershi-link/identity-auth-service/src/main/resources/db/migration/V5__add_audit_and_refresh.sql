-- 1. Create a Refresh Token table to track sessions
CREATE TABLE master_schema.refresh_tokens (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_value TEXT NOT NULL,
    expiry_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_refresh FOREIGN KEY (user_id)
        REFERENCES master_schema.users(user_id) ON DELETE CASCADE
);

-- 2. Create a simple Audit Log table (if you want database-level tracking)
CREATE TABLE master_schema.audit_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    action VARCHAR(255) NOT NULL,
    resource_affected VARCHAR(100),
    timestamp TIMESTAMPTZ DEFAULT NOW(),
    ip_address VARCHAR(45)
);

CREATE INDEX idx_audit_user ON master_schema.audit_logs(user_id);