-- =========================================================================
-- V15: Seed Pricing & Tariff Engine Permissions
-- Grants TARIFF_VIEW and TARIFF_MANAGE to master_schema roles for multi-tenant SACCO administration.
-- =========================================================================

INSERT INTO master_schema.permissions (permission_id, resource, action, description, is_active) VALUES
('b1e2c3d4-e5f6-4a5b-8c7d-9e0f1a2b3c4d', 'TARIFF', 'VIEW', 'Authority to inspect transaction fee tariffs, calculation simulations, and withholding tax records.', TRUE),
('c2d3e4f5-a6b7-4c8d-9e0f-1a2b3c4d5e6f', 'TARIFF', 'MANAGE', 'Authority to create, update, and toggle active status of transaction fee tariffs and tax policies.', TRUE)
ON CONFLICT (resource, action) DO NOTHING;

-- Grant TARIFF_VIEW to administrative, operational, audit, and teller roles
INSERT INTO master_schema.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM master_schema.roles r
CROSS JOIN master_schema.permissions p
WHERE p.resource = 'TARIFF' AND p.action = 'VIEW'
  AND r.role_name IN ('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR', 'TELLER')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant TARIFF_MANAGE to management and administrator roles only
INSERT INTO master_schema.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM master_schema.roles r
CROSS JOIN master_schema.permissions p
WHERE p.resource = 'TARIFF' AND p.action = 'MANAGE'
  AND r.role_name IN ('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN')
ON CONFLICT (role_id, permission_id) DO NOTHING;
