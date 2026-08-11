-- =========================================================================
-- V12: Loan Management Service (LMS) Granular RBAC Permissions
-- =========================================================================

-- 1. Insert Loan Management Permissions into master_schema
INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
    ('018f3b23-9999-7c3d-be4f-000000000040', 'LOAN_ACCOUNT',  'VIEW',    'Authority to inspect active loan accounts and repayment schedules.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000041', 'LOAN_DISBURSE', 'PROCESS', 'Authority to disburse funds and activate loan accounts.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000042', 'LOAN_REPAYMENT', 'PROCESS', 'Authority to process loan repayment transactions.', TRUE)
ON CONFLICT (permission_id) DO UPDATE SET
    resource = EXCLUDED.resource,
    action = EXCLUDED.action,
    description = EXCLUDED.description;

-- 2. Assign Loan Management Permissions to SYSTEM ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000040'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000041'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000042')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Assign Loan Management Permissions to SUPER_ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000040'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000041'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000042')
ON CONFLICT (role_id, permission_id) DO NOTHING;

