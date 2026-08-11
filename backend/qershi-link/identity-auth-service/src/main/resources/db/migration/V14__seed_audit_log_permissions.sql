-- =========================================================================
-- V14: Seed Audit Log View Permissions
-- Enables AUDIT_LOG_VIEW authority across master_schema roles for compliance reporting.
-- =========================================================================

INSERT INTO master_schema.permissions (permission_id, resource, action, description, is_active) VALUES
('a1d9f8e7-3b2c-4d5e-6f7a-8b9c0d1e2f3a', 'AUDIT_LOG', 'VIEW', 'Authority to inspect security and core banking audit trail logs.', TRUE)
ON CONFLICT (resource, action) DO NOTHING;

INSERT INTO master_schema.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM master_schema.roles r
CROSS JOIN master_schema.permissions p
WHERE p.resource = 'AUDIT_LOG' AND p.action = 'VIEW'
ON CONFLICT (role_id, permission_id) DO NOTHING;
