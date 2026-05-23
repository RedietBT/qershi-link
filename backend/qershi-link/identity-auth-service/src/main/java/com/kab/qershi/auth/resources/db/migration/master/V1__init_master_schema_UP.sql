-- 1. Create independent Enumerations
CREATE TYPE master_schema_sacco_status AS ENUM ('ACTIVE', 'SUSPENDED', 'PENDING_SETUP');
CREATE TYPE master_schema_global_role AS ENUM ('SYSTEM_ADMIN', 'UNION_ADMIN', 'UNION_AUDITOR', 'SACCO_USER');
CREATE TYPE master_schema_user_status AS ENUM ('ACTIVE', 'PENDING_SHARE', 'PENDING_APPROVAL', 'DEACTIVATED');

-- 2. Create SACCO Registry Table
CREATE TABLE master_schema.sacco_registry (
    sacco_id UUID PRIMARY KEY,
    parent_union_id UUID,
    sacco_name VARCHAR(255) NOT NULL,
    schema_name VARCHAR(63) NOT NULL UNIQUE,
    is_union BOOLEAN DEFAULT FALSE,
    min_share_requirement DECIMAL(19,4) DEFAULT 0.0000,
    status master_schema_sacco_status NOT NULL DEFAULT 'PENDING_SETUP',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_parent_union FOREIGN KEY (parent_union_id)
        REFERENCES master_schema.sacco_registry(sacco_id) ON DELETE RESTRICT
);

-- 3. Create Global Users Table
CREATE TABLE master_schema.users (
    user_id UUID PRIMARY KEY,
    msisdn VARCHAR(15) NOT NULL UNIQUE,
    sacco_id UUID NOT NULL,
    credential_hash TEXT NOT NULL,
    global_role master_schema_global_role NOT NULL,
    status master_schema_user_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    failed_login_attempts INT DEFAULT 0,
    last_login_at TIMESTAMPTZ,

    CONSTRAINT fk_user_sacco_registry FOREIGN KEY (sacco_id)
        REFERENCES master_schema.sacco_registry(sacco_id)
);

-- 4. High-Speed Lookups Performance Indexing (Critical Constraints section 3)
CREATE INDEX idx_users_msisdn ON master_schema.users(msisdn);
CREATE INDEX idx_sacco_parent_union ON master_schema.sacco_registry(parent_union_id);