-- =========================================================================
-- V13: Enhance Master Schema Systemic Security Audit Log Table
-- Supports tracking global authentication events, logins, failures, and SACCO onboardings.
-- =========================================================================

ALTER TABLE master_schema.audit_logs
    ADD COLUMN IF NOT EXISTS sacco_id UUID,
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    ADD COLUMN IF NOT EXISTS details TEXT;

CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON master_schema.audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON master_schema.audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_sacco_id ON master_schema.audit_logs(sacco_id);
