-- =========================================================================
-- V5: Chart of Accounts (COA) & System Business Date Tables
-- =========================================================================

-- 1. Enum for General Ledger Account Types
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gl_account_type') THEN
    CREATE TYPE gl_account_type AS ENUM ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'); END IF; END $$;

-- 2. System Business Date & EOD Coordination Tables
CREATE TABLE IF NOT EXISTS system_business_date (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    current_business_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    is_month_end BOOLEAN NOT NULL DEFAULT FALSE,
    last_eod_completed_at TIMESTAMPTZ,
    updated_by_user_id UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS eod_batch_executions (
    batch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_date DATE NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    triggered_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM_CRON',
    triggered_by_user_id UUID,
    total_accounts_accrued INT NOT NULL DEFAULT 0,
    total_interest_accrued DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    total_loans_evaluated INT NOT NULL DEFAULT 0,
    total_accounts_dormant INT NOT NULL DEFAULT 0,
    summary_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS eod_batch_step_logs (
    step_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    duration_ms BIGINT NOT NULL DEFAULT 0,
    records_affected INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_ebsl_batch_id FOREIGN KEY (batch_id) REFERENCES eod_batch_executions(batch_id) ON DELETE CASCADE
);

-- 3. Dynamic Chart of Accounts Master Table
CREATE TABLE IF NOT EXISTS chart_of_accounts (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gl_code VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(150) NOT NULL,
    account_type gl_account_type NOT NULL,
    parent_gl_code VARCHAR(50),
    currency VARCHAR(3) NOT NULL DEFAULT 'ETB',
    balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    is_reconciled BOOLEAN NOT NULL DEFAULT TRUE,
    allow_manual_journal BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_coa_parent_gl_code ON chart_of_accounts(parent_gl_code);
CREATE INDEX IF NOT EXISTS idx_coa_account_type ON chart_of_accounts(account_type);
CREATE INDEX IF NOT EXISTS idx_coa_status ON chart_of_accounts(status);

-- 4. Initial Seeds
INSERT INTO system_business_date (current_business_date, status, is_month_end)
SELECT CURRENT_DATE, 'OPEN', FALSE
WHERE NOT EXISTS (SELECT 1 FROM system_business_date);

-- Standard Cooperative Banking Chart of Accounts Seed
INSERT INTO chart_of_accounts (gl_code, account_name, account_type, parent_gl_code, description) VALUES
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
