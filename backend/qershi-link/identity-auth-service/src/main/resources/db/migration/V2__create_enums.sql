-- =========================================================================
-- V2: PostgreSQL Custom ENUM Types
-- Must exactly match the Java domain enums in the domain/model package.
-- These are schema-qualified to master_schema automatically by Flyway context.
-- =========================================================================

-- User account lifecycle states
-- PASSWORD_CHANGE_REQUIRED: forces a PIN rotation on first login
CREATE TYPE master_schema_user_status AS ENUM (
    'PENDING',
    'PENDING_APPROVAL',
    'PENDING_SHARE',
    'PASSWORD_CHANGE_REQUIRED',
    'ACTIVE',
    'BLOCKED',
    'DEACTIVATED'
);

-- Global platform role identifiers (system-wide, not tenant-scoped)
CREATE TYPE master_schema_global_role AS ENUM (
    'SUPER_ADMIN',
    'SACCO_ADMIN',
    'UNION_ADMIN',
    'SACCO_USER',
    'TELLER',
    'MEMBER'
);

-- SACCO workspace lifecycle states
CREATE TYPE master_schema_sacco_status AS ENUM (
    'PENDING_SETUP',
    'PENDING',
    'ACTIVE',
    'SUSPENDED',
    'INACTIVE'
);
