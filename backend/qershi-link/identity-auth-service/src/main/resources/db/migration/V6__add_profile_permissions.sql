-- =========================================================================
-- V6: Add Granular Profile Service Permissions
-- Seeds permissions for Member Profiles, KYC Verifications, and Next of Kin.
-- =========================================================================

-- 1. Insert Profile Service Permissions
INSERT INTO permissions (permission_id, resource, action, description, is_active) VALUES
('f1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c', 'MEMBER',      'UPDATE',          'Authority to update member demographics, address, employment, and status.', TRUE),
('f2a3b4c5-d6e7-8f9a-0b1c-2d3e4f5a6b7c', 'MEMBER',      'APPROVE',         'Four-Eye Principle supervisor authority to approve member onboarding.', TRUE),
('f3a4b5c6-d7e8-9f0a-1b2c-3d4e5f6a7b8c', 'KYC',         'SUBMIT',          'Authority to submit government ID verification documents for a member.', TRUE),
('f4a5b6c7-d8e9-0f1a-2b3c-4d5e6f7a8b9c', 'KYC',         'VIEW',            'Authority to view member government ID verification records.', TRUE),
('f5a6b7c8-d9e0-1f2a-3b4c-5d6e7f8a9b0c', 'KYC',         'VERIFY',          'Supervisor authority to approve or reject government ID verification records.', TRUE),
('f6a7b8c9-d0e1-2f3a-4b5c-6d7e8f9a0b1c', 'NEXT_OF_KIN', 'MANAGE',          'Authority to add, update, or remove nominated beneficiaries and allocations.', TRUE),
('f7a8b9c0-d1e2-3f4a-5b6c-7d8e9f0a1b2c', 'NEXT_OF_KIN', 'VIEW',            'Authority to view nominated beneficiary allocations for a member.', TRUE)
ON CONFLICT (resource, action) DO NOTHING;

-- 2. Link all new non-platform permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', permission_id
FROM permissions
WHERE resource IN ('MEMBER', 'KYC', 'NEXT_OF_KIN') AND is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Link all new permissions to SUPER_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0e1f3a2-4c5d-6e7f-8a9b-0c1d2e3f4a5b', permission_id
FROM permissions
WHERE resource IN ('MEMBER', 'KYC', 'NEXT_OF_KIN') AND is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
