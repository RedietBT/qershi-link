-- 08_seed_data.sql: Default System Roles and Platform Permissions Seed
INSERT INTO {schema}.permissions (resource, action, description) VALUES
('MEMBER',           'CREATE',       'Authority to register and onboard new SACCO members.'),
('MEMBER',           'VIEW_BASIC',   'Authority to view basic profiles of SACCO members.'),
('LOAN_REQUEST',     'CREATE',       'Authority to initiate a new loan request application.'),
('LOAN',             'APPROVE',      'Authority to review and formally approve applied loans.'),
('LOAN_APPLICATION', 'CREATE',       'Authority to submit new individual or group loan applications.'),
('LOAN_APPLICATION', 'VIEW',         'Authority to inspect loan applications and scoring profiles.'),
('LOAN_APPLICATION', 'APPROVE',      'Authority to execute Maker-Checker final loan approval.'),
('LOAN_GROUP',       'MANAGE',       'Authority to onboard and configure SACCO borrowing groups.'),
('LOAN_ACCOUNT',     'VIEW',         'Authority to inspect active loan accounts and repayment schedules.'),
('LOAN_DISBURSE',    'PROCESS',      'Authority to disburse funds and activate loan accounts.'),
('LOAN_REPAYMENT',   'PROCESS',      'Authority to process loan repayment transactions.'),
('CASH',             'DEPOSIT',      'Authority to process over-the-counter cash deposits.'),
('SAVINGS',          'WITHDRAW',     'Authority to process savings withdrawal requests.'),
('REPORT',           'VIEW_ALL',     'Authority to run and view overall SACCO financial reports.'),
('AUDIT_LOG',        'VIEW',         'Authority to inspect security and core banking audit trail logs.'),
('SACCO',            'ATTACH',       'Authority to link external core modules or sub-entities.'),
('NEXT_OF_KIN',      'VIEW',         'Authority to view member next of kin beneficiaries.'),
('NEXT_OF_KIN',      'MANAGE',       'Authority to add, update, or remove member next of kin beneficiaries.'),
('USER',             'VIEW_ALL',     'Authority to list and view all user security accounts.'),
('BRANCH',           'VIEW',         'Authority to view SACCO branches.'),
('BRANCH',           'MANAGE',       'Authority to create and configure SACCO branches.'),
('TELLER_TILL',      'VIEW',         'Authority to inspect teller cash drawers and cash positions.'),
('TELLER_TILL',      'MANAGE',       'Authority to open, close, and reconcile teller cash drawers.'),
('EOD',              'VIEW',         'Authority to inspect End-of-Day batch status and execution logs.'),
('EOD',              'EXECUTE',      'Supervisor authority to trigger End-of-Day batch processing.'),
('LOAN_DELINQUENCY', 'VIEW',         'Authority to view Portfolio at Risk (PAR) and loan delinquency dashboard.')
ON CONFLICT (resource, action) DO NOTHING;

INSERT INTO {schema}.roles (role_id, role_name, is_system_defined) VALUES
('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', 'ADMIN', TRUE),
('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e10', 'SACCO_ADMIN', TRUE)
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO {schema}.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM {schema}.roles r CROSS JOIN {schema}.permissions p
WHERE p.is_active = TRUE AND r.role_name IN ('ADMIN', 'SACCO_ADMIN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed default Head Office Main Branch for every new SACCO
INSERT INTO {schema}.branches (branch_code, branch_name, region, address, vault_gl_code, discretionary_lending_limit, status)
VALUES ('001', 'Head Office Main Branch', 'Headquarters', 'Main Office Complex', '1010-001', 500000.0000, 'ACTIVE')
ON CONFLICT (branch_code) DO NOTHING;

-- Seed initial Core Banking Business Date for new SACCO
INSERT INTO {schema}.system_business_date (current_business_date, status, is_month_end)
SELECT CURRENT_DATE, 'OPEN', FALSE
WHERE NOT EXISTS (SELECT 1 FROM {schema}.system_business_date);

