-- =========================================================================
-- V16: Seed SMS Gateway Configuration RBAC Permissions
-- =========================================================================

INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
    ('018f3b23-9999-7c3d-be4f-000000000023', 'NOTIFICATION', 'CONFIG_MANAGE', 'Authority to configure SACCO SMS gateway providers and credentials.', TRUE)
ON CONFLICT (permission_id) DO UPDATE SET
    resource = EXCLUDED.resource,
    action = EXCLUDED.action,
    description = EXCLUDED.description;

-- Assign to SYSTEM ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000023')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Assign to SUPER_ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000023')
ON CONFLICT (role_id, permission_id) DO NOTHING;
