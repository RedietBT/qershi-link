-- 1. Create the Permission
INSERT INTO master_schema.permissions (permission_id, resource, action, description, is_active, created_at)
VALUES ('c1a2b3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'PLATFORM', 'MANAGE_SACCO', 'Allows monitoring and management of all tenant registries.', TRUE, NOW());

-- 2. Create the Role
INSERT INTO master_schema.roles (role_id, role_name, is_system_defined, created_at)
VALUES ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', 'SUPER_ADMIN', TRUE, NOW());

-- 3. Link them (Assign permission to the role)
INSERT INTO master_schema.role_permissions (role_id, permission_id)
VALUES ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', 'c1a2b3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d');