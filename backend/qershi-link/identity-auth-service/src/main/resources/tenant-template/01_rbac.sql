-- 01_rbac.sql: Tenant RBAC and Security Schema Definitions
CREATE SCHEMA IF NOT EXISTS {schema};

CREATE TABLE IF NOT EXISTS {schema}.roles (
    role_id UUID PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    is_system_defined BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.permissions (
    permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_{schema}_res_act UNIQUE (resource, action)
);

CREATE TABLE IF NOT EXISTS {schema}.role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES {schema}.roles(role_id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES {schema}.permissions(permission_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    sacco_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id, sacco_id),
    FOREIGN KEY (role_id) REFERENCES {schema}.roles(role_id) ON DELETE CASCADE
);
