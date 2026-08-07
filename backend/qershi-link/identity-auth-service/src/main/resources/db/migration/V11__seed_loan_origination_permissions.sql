-- =========================================================================
-- V11: Loan Origination Service (LOS) Granular RBAC Permissions
-- =========================================================================

-- 1. Insert Loan Origination Permissions into master_schema
INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
    ('018f3b23-9999-7c3d-be4f-000000000030', 'LOAN_APPLICATION', 'CREATE',   'Authority to submit new individual or group loan applications.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000031', 'LOAN_APPLICATION', 'VIEW',     'Authority to inspect loan applications and scoring profiles.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000032', 'LOAN_APPLICATION', 'APPROVE',  'Authority to execute Maker-Checker final loan approval.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000033', 'LOAN_GROUP',       'MANAGE',   'Authority to onboard and configure SACCO borrowing groups.', TRUE)
ON CONFLICT (permission_id) DO UPDATE SET
    resource = EXCLUDED.resource,
    action = EXCLUDED.action,
    description = EXCLUDED.description;

-- 2. Assign Loan Origination Permissions to SYSTEM ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000030'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000031'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000032'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000033')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Assign Loan Origination Permissions to SUPER_ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000030'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000031'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000032'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000033')
ON CONFLICT (role_id, permission_id) DO NOTHING;
