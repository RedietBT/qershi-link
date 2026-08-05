-- =========================================================================
-- V9: Transaction & Journal Posting Engine Granular RBAC Permissions
-- =========================================================================

-- 1. Insert Transaction Permissions into master_schema
INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
    ('018f3b23-9999-7c3d-be4f-000000000010', 'TRANSACTION', 'DEPOSIT',   'Authority to process over-the-counter teller cash deposits.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000011', 'TRANSACTION', 'WITHDRAW',  'Authority to process member cash withdrawals.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000012', 'TRANSACTION', 'TRANSFER',  'Authority to process member-to-member internal transfers.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000013', 'TRANSACTION', 'VIEW',      'Authority to view transaction histories and GL journal lines.', TRUE),
    ('018f3b23-9999-7c3d-be4f-000000000014', 'TRANSACTION', 'REVERSE',   'Authority to approve and execute transaction reversals.', TRUE)
ON CONFLICT (permission_id) DO UPDATE SET
    resource = EXCLUDED.resource,
    action = EXCLUDED.action,
    description = EXCLUDED.description;

-- 2. Assign Transaction Permissions to SYSTEM ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000010'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000011'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000012'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000013'),
    ('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', '018f3b23-9999-7c3d-be4f-000000000014')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Assign Transaction Permissions to SUPER_ADMIN Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000010'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000011'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000012'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000013'),
    ('b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', '018f3b23-9999-7c3d-be4f-000000000014')
ON CONFLICT (role_id, permission_id) DO NOTHING;
