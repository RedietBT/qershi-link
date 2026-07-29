-- =========================================================================
-- V7: Add Granular Role Management Permissions
-- Seeds permissions for creating, reading, updating, and deleting custom roles.
-- =========================================================================

-- 1. Insert Role Management Permissions into master_schema
INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
('e1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c', 'ROLE', 'READ',   'Authority to view custom roles and permission definitions.', TRUE),
('e2a3b4c5-d6e7-8f9a-0b1c-2d3e4f5a6b7c', 'ROLE', 'CREATE', 'Authority to create new custom local tenant roles.', TRUE),
('e3a4b5c6-d7e8-9f0a-1b2c-3d4e5f6a7b8c', 'ROLE', 'UPDATE', 'Authority to modify role names and assign/remove permissions.', TRUE),
('e4a5b6c7-d8e9-0f1a-2b3c-4d5e6f7a8b9c', 'ROLE', 'DELETE', 'Authority to safely delete custom local tenant roles.', TRUE),
('e5a6b7c8-d9e0-1f2a-3b4c-5d6e7f8a9b0c', 'ROLE', 'MANAGE', 'Full administrative authority to manage system and custom roles.', TRUE)
ON CONFLICT (resource, action) DO NOTHING;

-- 2. Link new ROLE permissions to system ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', permission_id
FROM permissions
WHERE resource = 'ROLE' AND is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Link new ROLE permissions to SUPER_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', permission_id
FROM permissions
WHERE resource = 'ROLE' AND is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
