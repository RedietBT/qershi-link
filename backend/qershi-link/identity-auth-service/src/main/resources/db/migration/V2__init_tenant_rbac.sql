-- =========================================================================
-- 1. Create Roles Table
-- =========================================================================
CREATE TABLE roles (
    role_id UUID PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    is_system_defined BOOLEAN DEFAULT FALSE, -- If TRUE, cannot be modified/deleted by local users
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================================================================
-- 2. Create Permissions Table (Resource & Action Driven)
-- =========================================================================
CREATE TABLE permissions (
    permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource VARCHAR(50) NOT NULL,            -- e.g., 'MEMBER', 'LOAN', 'CASH'
    action VARCHAR(50) NOT NULL,              -- e.g., 'CREATE', 'APPROVE', 'DEPOSIT'
    description VARCHAR(255),                 -- Human-readable description
    is_active BOOLEAN NOT NULL DEFAULT TRUE,  -- Allows toggling capabilities globally
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_resource_action UNIQUE (resource, action)
);

-- =========================================================================
-- 3. Create Role Permissions Link Table (Bridge Table)
-- =========================================================================
CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-- =========================================================================
-- 4. Create User Roles Bridge Table
-- =========================================================================
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

-- =========================================================================
-- 5. Seed Core System Permissions Matrix
-- =========================================================================
INSERT INTO permissions (resource, action, description, is_active) VALUES
('MEMBER',        'CREATE',       'Authority to register and onboard new SACCO members.', TRUE),
('MEMBER',        'VIEW_BASIC',   'Authority to view basic profiles of SACCO members.', TRUE),
('LOAN_REQUEST',  'CREATE',       'Authority to initiate a new loan request application.', TRUE),
('LOAN',          'APPROVE',      'Authority to review and formally approve applied loans.', TRUE),
('CASH',          'DEPOSIT',      'Authority to process over-the-counter cash deposits.', TRUE),
('SAVINGS',       'WITHDRAW',     'Authority to process savings withdrawal requests.', TRUE),
('REPORT',        'VIEW_ALL',     'Authority to run and view overall SACCO financial reports.', TRUE),
('SACCO',         'ATTACH',       'Authority to link external core modules or sub-entities.', TRUE);

-- =========================================================================
-- 6. Seed System-Defined Roles
--    Using a fixed standard UUID for the ADMIN role across all schemas.
-- =========================================================================
INSERT INTO roles (role_id, role_name, is_system_defined) VALUES
('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', 'ADMIN', TRUE);

-- =========================================================================
-- 7. Grant ALL Active Permissions to ADMIN Role Automatically
-- =========================================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', permission_id
FROM permissions
WHERE is_active = TRUE;