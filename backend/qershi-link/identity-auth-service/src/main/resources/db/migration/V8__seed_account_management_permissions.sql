-- =========================================================================
-- V8: Add Account Management Service Permissions
-- Seeds permissions for Account Opening, Approvals, Product Factory,
-- and Lien Hold management used by the account-management-service.
-- =========================================================================

-- 1. Insert Account Management Permissions
INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'ACCOUNT', 'OPEN',      'Authority to open a new member savings or deposit account.', TRUE),
('a2b3c4d5-e6f7-8a9b-0c1d-2e3f4a5b6c7d', 'ACCOUNT', 'APPROVE',   'Four-Eye supervisor authority to approve pending account openings.', TRUE),
('a3b4c5d6-e7f8-9a0b-1c2d-3e4f5a6b7c8d', 'ACCOUNT', 'VIEW',      'Authority to view account ledger balances and status details.', TRUE),
('a4b5c6d7-e8f9-0a1b-2c3d-4e5f6a7b8c9d', 'ACCOUNT', 'VIEW_ALL',  'Authority to list all member accounts within the active SACCO tenant.', TRUE),
('a5b6c7d8-e9f0-1a2b-3c4d-5e6f7a8b9c0d', 'ACCOUNT', 'FREEZE',    'Authority to administratively freeze or unfreeze a member account.', TRUE),
('a6b7c8d9-e0f1-2a3b-4c5d-6e7f8a9b0c1d', 'PRODUCT', 'CREATE',    'Authority to configure and define new SACCO dynamic deposit products.', TRUE),
('a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d', 'PRODUCT', 'VIEW',      'Authority to view SACCO deposit product definitions and rules.', TRUE),
('a8b9c0d1-e2f3-4a5b-6c7d-8e9f0a1b2c3d', 'LIEN',    'CREATE',    'Authority to place a partial monetary lien hold on a member account.', TRUE),
('a9b0c1d2-e3f4-5a6b-7c8d-9e0f1a2b3c4d', 'LIEN',    'RELEASE',   'Supervisor authority to release an active monetary lien hold.', TRUE),
('b0b1c2d3-e4f5-6a7b-8c9d-0e1f2a3b4c5d', 'LIEN',    'VIEW',      'Authority to view active lien holds on member accounts.', TRUE)
ON CONFLICT (resource, action) DO NOTHING;

-- 2. Assign all new Account Management permissions to the ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', permission_id
FROM permissions
WHERE resource IN ('ACCOUNT', 'PRODUCT', 'LIEN') AND is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Assign all new Account Management permissions to the SUPER_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', permission_id
FROM permissions
WHERE resource IN ('ACCOUNT', 'PRODUCT', 'LIEN') AND is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
