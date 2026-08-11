-- =========================================================================
-- V3: Master Schema Core Tables
-- Stores global user identities and SACCO tenant registry entries.
-- These tables are shared across all tenants — they live in master_schema.
-- =========================================================================

-- 1. SACCO Registry — the tenant directory
--    Every user, every role assignment, every schema belongs to a SACCO entry.
CREATE TABLE IF NOT EXISTS sacco_registry (
    sacco_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_union_id      UUID,
    sacco_name           VARCHAR(255) NOT NULL,
    schema_name          VARCHAR(63)  NOT NULL UNIQUE,
    is_union             BOOLEAN      NOT NULL DEFAULT FALSE,
    min_share_requirement DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    status               master_schema_sacco_status NOT NULL DEFAULT 'PENDING_SETUP',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    -- A union SACCO can be a parent of other SACCOs
    CONSTRAINT fk_parent_union FOREIGN KEY (parent_union_id)
        REFERENCES sacco_registry(sacco_id) ON DELETE RESTRICT
);

-- 2. Global User Identity Table
--    Stores authentication credentials and global role for every user in the system.
--    Demographic profile data lives in the separate profile-service.
CREATE TABLE IF NOT EXISTS users (
    user_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msisdn               VARCHAR(15)  NOT NULL UNIQUE,
    sacco_id             UUID         NOT NULL,
    credential_hash      TEXT         NOT NULL,
    global_role          master_schema_global_role   NOT NULL,
    status               master_schema_user_status   NOT NULL DEFAULT 'PENDING_APPROVAL',
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    last_login_at        TIMESTAMPTZ,

    CONSTRAINT fk_user_sacco FOREIGN KEY (sacco_id)
        REFERENCES sacco_registry(sacco_id)
);

-- 3. Session Refresh Token Store
--    Tracks active JWT refresh sessions per user.
CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    token_value TEXT         NOT NULL,
    expiry_date TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. Audit Trail Log
--    Records key security and administrative actions for compliance.
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID,
    action            VARCHAR(255) NOT NULL,
    resource_affected VARCHAR(100),
    timestamp         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ip_address        VARCHAR(45)
);
