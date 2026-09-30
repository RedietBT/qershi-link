-- 03_account.sql: Account Products, Accounts, and Liens
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'account_status') THEN
    CREATE TYPE account_status AS ENUM ('PENDING_APPROVAL', 'ACTIVE', 'DORMANT', 'FROZEN', 'CLOSED'); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'freeze_status') THEN
    CREATE TYPE freeze_status AS ENUM ('NONE', 'DEBIT_FREEZE', 'CREDIT_FREEZE', 'FULL_FREEZE'); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lien_status') THEN
    CREATE TYPE lien_status AS ENUM ('ACTIVE', 'RELEASED', 'EXPIRED'); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'interest_posting_frequency') THEN
    CREATE TYPE interest_posting_frequency AS ENUM ('MONTHLY', 'QUARTERLY', 'SEMI_ANNUALLY', 'ANNUALLY', 'AT_MATURITY'); END IF; END $$;

CREATE TABLE IF NOT EXISTS {schema}.account_products (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_code VARCHAR(10) NOT NULL UNIQUE,
    product_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'ETB',
    interest_rate_pa DECIMAL(7,4) NOT NULL DEFAULT 0.0000,
    posting_frequency interest_posting_frequency NOT NULL DEFAULT 'MONTHLY',
    min_operating_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    min_monthly_contribution DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    term_period_months INT,
    early_withdrawal_penalty_pct DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.branches (
    branch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_code VARCHAR(10) NOT NULL UNIQUE,
    branch_name VARCHAR(150) NOT NULL,
    region VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    contact_phone VARCHAR(30),
    manager_user_id UUID,
    vault_gl_code VARCHAR(50) NOT NULL DEFAULT '1010-001',
    discretionary_lending_limit DECIMAL(19,4) NOT NULL DEFAULT 100000.0000,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.accounts (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    sacco_code VARCHAR(20) NOT NULL,
    branch_code VARCHAR(20) NOT NULL,
    product_code VARCHAR(10) NOT NULL,
    book_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    lien_hold_amount DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    accrued_interest_payable DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    last_interest_accrual_date DATE,
    last_capitalization_date DATE,
    last_activity_date DATE DEFAULT CURRENT_DATE,
    status account_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    freeze_status freeze_status NOT NULL DEFAULT 'NONE',
    opened_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approved_by_user_id UUID,
    approval_date TIMESTAMPTZ,
    closed_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (product_code) REFERENCES {schema}.account_products(product_code) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.account_liens (
    lien_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no VARCHAR(50) NOT NULL,
    lien_amount DECIMAL(19,4) NOT NULL,
    reason TEXT NOT NULL,
    reference_no VARCHAR(100),
    placed_by_user_id UUID NOT NULL,
    released_by_user_id UUID,
    status lien_status NOT NULL DEFAULT 'ACTIVE',
    placed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at TIMESTAMPTZ,
    FOREIGN KEY (account_no) REFERENCES {schema}.accounts(account_no) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.account_audit_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no VARCHAR(50),
    user_id UUID NOT NULL,
    performed_by_user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================================================================
-- Day 2: Core Banking Business Date & End-of-Day (EOD) Batch Coordination
-- =========================================================================
CREATE TABLE IF NOT EXISTS {schema}.system_business_date (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    current_business_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'CUTOFF_LOCKED', 'PROCESSING_EOD', 'CLOSED'
    is_month_end BOOLEAN NOT NULL DEFAULT FALSE,
    last_eod_completed_at TIMESTAMPTZ,
    updated_by_user_id UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.eod_batch_executions (
    batch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_date DATE NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS', -- 'IN_PROGRESS', 'COMPLETED', 'FAILED'
    triggered_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM_CRON', -- 'SYSTEM_CRON', 'MANUAL_OVERRIDE'
    triggered_by_user_id UUID,
    total_accounts_accrued INT NOT NULL DEFAULT 0,
    total_interest_accrued DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    total_loans_evaluated INT NOT NULL DEFAULT 0,
    total_accounts_dormant INT NOT NULL DEFAULT 0,
    summary_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.eod_batch_step_logs (
    step_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL,
    step_name VARCHAR(100) NOT NULL, -- 'CUTOFF_LOCK', 'SAVINGS_INTEREST_ACCRUAL', 'INTEREST_CAPITALIZATION', 'LOAN_PAR_AGING', 'DORMANCY_SWEEP', 'DATE_ROLLOVER'
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- 'SUCCESS', 'FAILED', 'SKIPPED'
    duration_ms BIGINT NOT NULL DEFAULT 0,
    records_affected INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (batch_id) REFERENCES {schema}.eod_batch_executions(batch_id) ON DELETE CASCADE
);

-- =========================================================================
-- Day 3: Dynamic Chart of Accounts (COA)
-- =========================================================================
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gl_account_type') THEN
    CREATE TYPE gl_account_type AS ENUM ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'); END IF; END $$;

CREATE TABLE IF NOT EXISTS {schema}.chart_of_accounts (
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

CREATE INDEX IF NOT EXISTS idx_coa_parent_gl_code ON {schema}.chart_of_accounts(parent_gl_code);
CREATE INDEX IF NOT EXISTS idx_coa_account_type ON {schema}.chart_of_accounts(account_type);
CREATE INDEX IF NOT EXISTS idx_coa_status ON {schema}.chart_of_accounts(status);


