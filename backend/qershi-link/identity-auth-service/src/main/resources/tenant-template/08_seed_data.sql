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
('LOAN_DELINQUENCY', 'VIEW',         'Authority to view Portfolio at Risk (PAR) and loan delinquency dashboard.'),
('COA',              'VIEW',         'Authority to view Chart of Accounts tree and GL account balances.'),
('COA',              'MANAGE',       'Authority to create and configure Chart of Accounts.'),
('FINANCIAL_REPORT', 'VIEW',         'Authority to view Trial Balance, Balance Sheet, and Profit & Loss reports.'),
('TARIFF',           'VIEW',         'Authority to inspect transaction fee tariffs and withholding tax records.'),
('TARIFF',           'MANAGE',       'Authority to configure and toggle fee tariffs and tax policies.')
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

-- Seed Standard Cooperative Banking Chart of Accounts (WOCCU / Temenos / Mifos Model)
INSERT INTO {schema}.chart_of_accounts (gl_code, account_name, account_type, parent_gl_code, description) VALUES
-- 1000: ASSETS
('1000', 'Assets', 'ASSET', NULL, 'Master asset control category'),
('1010', 'Cash and Cash Equivalents', 'ASSET', '1000', 'Physical cash holdings and till reserves'),
('1011', 'Vault Cash', 'ASSET', '1010', 'Head office and branch main vault cash'),
('1012', 'Teller Tills', 'ASSET', '1010', 'Counter cash held by active tellers'),
('1013', 'Petty Cash', 'ASSET', '1010', 'Office petty cash float'),
('1020', 'Bank Balances & Clearing', 'ASSET', '1000', 'Commercial bank checking and clearing accounts'),
('1021', 'Commercial Bank Operating Account', 'ASSET', '1020', 'Primary operational bank account'),
('1022', 'Apex Union / Central Reserve Deposit', 'ASSET', '1020', 'Mandatory liquidity reserve at SACCO Union'),
('1100', 'Loans to Members (Portfolio)', 'ASSET', '1000', 'Outstanding principal on active loans'),
('1110', 'Standard Amortized Loans', 'ASSET', '1100', 'Regular term loans portfolio'),
('1120', 'Emergency / Instant Loans', 'ASSET', '1100', 'Short-term fast liquidity loans'),
('1130', 'Agriculture & Business Loans', 'ASSET', '1100', 'Commercial and farming credit facility'),
('1190', 'Allowance for Loan Impairment', 'ASSET', '1100', 'Contra-asset provision for non-performing loans'),

-- 2000: LIABILITIES
('2000', 'Liabilities', 'LIABILITY', NULL, 'Master liabilities control category'),
('2010', 'Member Deposits', 'LIABILITY', '2000', 'Member savings and liquid accounts'),
('2011', 'Regular Compulsory Savings', 'LIABILITY', '2010', 'Monthly mandatory member savings deposits'),
('2012', 'Voluntary Savings Deposits', 'LIABILITY', '2010', 'Withdrawable on-demand member savings'),
('2013', 'Term / Fixed Deposits', 'LIABILITY', '2010', 'Fixed-term interest-bearing deposits'),
('2050', 'Accrued Interest Payable', 'LIABILITY', '2000', 'Accumulated uncapitalized interest owed to members'),
('2051', 'Accrued Savings Interest Payable', 'LIABILITY', '2050', 'Daily accruals pending monthly capitalization'),
('2090', 'Other Payables & Provisions', 'LIABILITY', '2000', 'Operational payables and statutory withholdings'),
('2091', 'Withholding Tax (WHT) Payable', 'LIABILITY', '2090', 'Government tax withheld on member interest'),

-- 3000: EQUITY
('3000', 'Equity & Capital', 'EQUITY', NULL, 'Master equity and institutional capital category'),
('3010', 'Member Share Capital', 'EQUITY', '3000', 'Permanent ownership shares purchased by members'),
('3011', 'Mandatory Membership Shares', 'EQUITY', '3010', 'Statutory qualifying membership shares'),
('3012', 'Voluntary Additional Shares', 'EQUITY', '3010', 'Secondary non-withdrawable member shares'),
('3020', 'Statutory Reserves', 'EQUITY', '3000', 'Non-distributable statutory reserve fund'),
('3021', 'Statutory Legal Reserve Fund', 'EQUITY', '3020', 'Mandatory 20-25% legal annual reserve'),
('3030', 'Retained Surplus / Earnings', 'EQUITY', '3000', 'Accumulated undistributed cooperative profits'),
('3031', 'Undistributed Prior Years Surplus', 'EQUITY', '3030', 'Carried forward surplus from previous fiscal years'),
('3032', 'Current Year Net Surplus', 'EQUITY', '3030', 'Operating surplus for the active financial year'),

-- 4000: REVENUE
('4000', 'Revenue / Operating Income', 'REVENUE', NULL, 'Master revenue and earnings category'),
('4010', 'Interest Income from Loans', 'REVENUE', '4000', 'Interest earned from lending operations'),
('4011', 'Interest on Standard Loans', 'REVENUE', '4010', 'Amortized loan portfolio interest yield'),
('4012', 'Interest on Emergency Loans', 'REVENUE', '4010', 'Short-term loan financing charges'),
('4020', 'Fee and Commission Income', 'REVENUE', '4000', 'Transactional and administrative fee income'),
('4021', 'Loan Processing Fees', 'REVENUE', '4020', 'Origination, appraisal and disbursement fees'),
('4022', 'Membership Registration Fees', 'REVENUE', '4020', 'One-time onboarding entrance fees'),
('4023', 'Late Payment Penalties', 'REVENUE', '4020', 'Delinquency and default penalty levies'),

-- 5000: EXPENSES
('5000', 'Operating Expenses', 'EXPENSE', NULL, 'Master expense and operating cost category'),
('5010', 'Financial Expenses', 'EXPENSE', '5000', 'Costs incurred on financial liabilities'),
('5011', 'Interest Expense on Member Savings', 'EXPENSE', '5010', 'Yield paid or accrued to member savings accounts'),
('5012', 'Bank Service Charges', 'EXPENSE', '5010', 'Commercial bank fees and electronic transaction costs'),
('5020', 'Loan Loss & Impairment Expense', 'EXPENSE', '5000', 'Provisions allocated for bad and doubtful debts'),
('5021', 'Provision for Bad Debts Expense', 'EXPENSE', '5020', 'Monthly/annual regulatory loan loss provisioning charge'),
('5030', 'General Administrative Expenses', 'EXPENSE', '5000', 'SACCO office, overhead and operational costs'),
('5031', 'Staff Salaries & Benefits', 'EXPENSE', '5030', 'Employee payroll, allowances and pension'),
('5032', 'Office Rent & Utilities', 'EXPENSE', '5030', 'Branch rental, electric, internet and telecom'),
('5033', 'IT & Software Subscriptions', 'EXPENSE', '5030', 'Core banking infrastructure, hosting and licensing')
ON CONFLICT (gl_code) DO NOTHING;


