-- =========================================================================
-- V10: Notification & Messaging Service Granular RBAC Permissions
-- =========================================================================

-- 1. Insert Notification Permissions into master_schema
INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
    ('018f3b23-9999-7c3d-be4f-000000000020', 'NOTIFICATION', 'SEND',            'Authority to dispatch SMS/Email notifications to members.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000021', 'NOTIFICATION', 'TEMPLATE_MANAGE', 'Authority to create and manage custom notification templates.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000022', 'NOTIFICATION', 'LOG_VIEW',        'Authority to view notification delivery audit logs.', TRUE)
ON CONFLICT (permission_id) DO UPDATE SET
    resource = EXCLUDED.resource,
    action = EXCLUDED.action,
    description = EXCLUDED.description;

-- 2. Assign Notification Permissions to SYSTEM ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000020'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000021'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000022')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Assign Notification Permissions to SUPER_ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000020'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000021'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000022')
ON CONFLICT (role_id, permission_id) DO NOTHING;
