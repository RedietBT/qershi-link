-- =========================================================================
-- V5: Seed Initial Data
-- Seeds default permissions, system-defined roles, and initial system SACCO.
-- =========================================================================

-- 1. Insert System Platform SACCO entry (Required for Super Admin registration)
INSERT INTO sacco_registry (sacco_id, sacco_name, schema_name, status, is_union, min_share_requirement, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000000', 'SYSTEM_PLATFORM', 'master_schema', 'ACTIVE', FALSE, 0.0000, NOW(), NOW())
ON CONFLICT (sacco_id) DO UPDATE 
SET is_union = FALSE, min_share_requirement = 0.0000;

-- 2. Seed Core System Permissions
INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
('c1a2b3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'PLATFORM',      'MANAGE_SACCO', 'Allows monitoring and management of all tenant registries.', TRUE),
('d2a3b4c5-e6f7-5a6b-9c0d-1e2f3a4b5c6d', 'MEMBER',        'CREATE',       'Authority to register and onboard new SACCO members.', TRUE),
('d3a4b5c6-e7f8-6a7b-0c1d-2e3f4a5b6c7d', 'MEMBER',        'VIEW_BASIC',   'Authority to view basic profiles of SACCO members.', TRUE),
('d4a5b6c7-e8f9-7a8b-1c2d-3e4f5a6b7c8d', 'LOAN_REQUEST',  'CREATE',       'Authority to initiate a new loan request application.', TRUE),
('d5a6b7c8-e9f0-8a9b-2c3d-4e5f6a7b8c9d', 'LOAN',          'APPROVE',      'Authority to review and formally approve applied loans.', TRUE),
('d6a7b8c9-e0f1-9a0b-3c4d-5e6f7a8b9c0d', 'CASH',          'DEPOSIT',      'Authority to process over-the-counter cash deposits.', TRUE),
('d7a8b9c0-e1f2-0a1b-4c5d-6e7f8a9b0c1d', 'SAVINGS',       'WITHDRAW',     'Authority to process savings withdrawal requests.', TRUE),
('d8a9b0c1-e2f3-1a2b-5c6d-7e8f9a0b1c2d', 'REPORT',        'VIEW_ALL',     'Authority to run and view overall SACCO financial reports.', TRUE),
('d9a0b1c2-e3f4-2a3b-6c7d-8e9f0a1b2c3d', 'SACCO',         'ATTACH',       'Authority to link external core modules or sub-entities.', TRUE),
('e0a1b2c3-f4a5-3b4c-7d8e-9f0a1b2c3d4e', 'USER',          'VIEW_ALL',     'Authority to list and view all user security accounts.', TRUE),
('f1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5e', 'NOTIFICATION_TEMPLATE', 'MANAGE', 'Authority to configure and manage SACCO notification templates.', TRUE)
ON CONFLICT (resource, action) DO NOTHING;

-- 3. Seed System-Defined Roles
INSERT INTO roles (role_id, role_name, is_system_defined) VALUES
('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', 'ADMIN', TRUE),
('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', 'SUPER_ADMIN', TRUE)
ON CONFLICT (role_id) DO NOTHING;

-- 4. Assign All Non-Platform Permissions to ADMIN Role
INSERT INTO role_permissions (role_id, permission_id)
SELECT '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', permission_id
FROM permissions
WHERE resource != 'PLATFORM' AND is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 5. Assign PLATFORM_MANAGE_SACCO and All Permissions to SUPER_ADMIN Role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', permission_id
FROM permissions
WHERE is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
