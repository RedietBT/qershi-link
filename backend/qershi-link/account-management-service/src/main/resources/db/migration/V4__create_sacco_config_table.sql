-- =========================================================================
-- V4: SACCO Configuration & Code Table
-- Stores tenant-scoped SACCO code and default branch code settings.
-- =========================================================================

CREATE TABLE IF NOT EXISTS sacco_configs (
    config_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sacco_code  VARCHAR(20) NOT NULL UNIQUE,
    sacco_name  VARCHAR(200),
    branch_code VARCHAR(20) NOT NULL DEFAULT '0001',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sc_sacco_code ON sacco_configs(sacco_code);
