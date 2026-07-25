-- =========================================================================
-- V4: RBAC Tables (Role-Based Access Control)
-- These tables define the permission model for master_schema.
-- Each onboarded SACCO tenant gets its own copy in their private schema
-- (created dynamically by TenantProvisioningAdapter at runtime).
-- =========================================================================

-- 1. Roles — named bundles of permissions
--    system_defined = TRUE means this role cannot be modified by tenant admins.
CREATE TABLE roles (
    role_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name        VARCHAR(50) NOT NULL,
    is_system_defined BOOLEAN    NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Permissions — atomic capability units (resource + action pairs)
--    e.g. resource='MEMBER', action='CREATE' → authority string 'MEMBER_CREATE'
CREATE TABLE permissions (
    permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource      VARCHAR(50)  NOT NULL,
    action        VARCHAR(50)  NOT NULL,
    description   VARCHAR(255),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_resource_action UNIQUE (resource, action)
);

-- 3. Role → Permission mapping bridge table
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles(role_id)       ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-- 4. User → Role assignment bridge table
--    sacco_id scopes the role assignment to a specific tenant context.
--    This is the authoritative source queried by findAuthoritiesByUserIdAndSaccoId.
CREATE TABLE user_roles (
    user_id  UUID NOT NULL,
    role_id  UUID NOT NULL,
    sacco_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id, sacco_id),

    CONSTRAINT fk_ur_role  FOREIGN KEY (role_id)  REFERENCES roles(role_id)             ON DELETE CASCADE,
    CONSTRAINT fk_ur_sacco FOREIGN KEY (sacco_id) REFERENCES sacco_registry(sacco_id)   ON DELETE CASCADE
);
