-- =========================================================================
-- V16: Seed SMS Gateway Configuration & Complete Notification RBAC Permissions
-- Ensures all notification permissions are bound to SUPER_ADMIN, SACCO_ADMIN, and ADMIN.
-- =========================================================================

-- 1. Insert/Update NOTIFICATION:CONFIG_MANAGE permission
INSERT INTO master_schema.permissions (permission_id, resource, action, description, is_active) VALUES
    ('018f3b23-9999-7c3d-be4f-000000000023', 'NOTIFICATION', 'CONFIG_MANAGE', 'Authority to configure SACCO SMS gateway providers and credentials.', TRUE)
ON CONFLICT (resource, action) DO NOTHING;

-- 2. Grant NOTIFICATION_CONFIG_MANAGE to SUPER_ADMIN, ADMIN, and SACCO_ADMIN
INSERT INTO master_schema.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM master_schema.roles r
CROSS JOIN master_schema.permissions p
WHERE p.resource = 'NOTIFICATION' AND p.action = 'CONFIG_MANAGE'
  AND r.role_name IN ('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Grant NOTIFICATION_TEMPLATE_MANAGE and NOTIFICATION_SEND to SUPER_ADMIN, ADMIN, and SACCO_ADMIN
INSERT INTO master_schema.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM master_schema.roles r
CROSS JOIN master_schema.permissions p
WHERE p.resource = 'NOTIFICATION' AND p.action IN ('TEMPLATE_MANAGE', 'SEND')
  AND r.role_name IN ('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 4. Grant NOTIFICATION_LOG_VIEW to administrative and audit roles
INSERT INTO master_schema.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM master_schema.roles r
CROSS JOIN master_schema.permissions p
WHERE p.resource = 'NOTIFICATION' AND p.action = 'LOG_VIEW'
  AND r.role_name IN ('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR')
ON CONFLICT (role_id, permission_id) DO NOTHING;
