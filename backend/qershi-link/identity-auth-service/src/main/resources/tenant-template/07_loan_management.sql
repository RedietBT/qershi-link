-- 07_loan_management.sql: Loan Accounts, Schedules, Waterfalls & Repayments
CREATE TABLE IF NOT EXISTS {schema}.payment_channels (
    channel_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_code VARCHAR(50) NOT NULL UNIQUE,
    channel_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.repayment_frequencies (
    frequency_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    frequency_code VARCHAR(50) NOT NULL UNIQUE,
    frequency_name VARCHAR(100) NOT NULL,
    interval_unit VARCHAR(20) NOT NULL DEFAULT 'MONTHS',
    interval_count INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.interest_strategies (
    strategy_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_code VARCHAR(50) NOT NULL UNIQUE,
    strategy_name VARCHAR(100) NOT NULL,
    formula_type VARCHAR(50) NOT NULL DEFAULT 'REDUCING_BALANCE',
    day_count_convention VARCHAR(30) NOT NULL DEFAULT 'ACTUAL_365',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.loan_penalty_configs (
    config_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_code VARCHAR(50) NOT NULL UNIQUE,
    policy_name VARCHAR(100) NOT NULL,
    grace_period_days INT NOT NULL DEFAULT 5,
    penalty_rate_pct DECIMAL(5,2) NOT NULL DEFAULT 2.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.loan_accounts (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no VARCHAR(50) NOT NULL UNIQUE,
    application_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_rate_pct DECIMAL(5,2) NOT NULL,
    term_months INT NOT NULL,
    repayment_frequency VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    interest_type VARCHAR(50) NOT NULL DEFAULT 'REDUCING_BALANCE',
    disbursement_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(30) NOT NULL DEFAULT 'DISBURSED',
    days_past_due INT NOT NULL DEFAULT 0,
    par_bucket VARCHAR(30) NOT NULL DEFAULT 'CURRENT', -- 'CURRENT', 'WATCHLIST_PAR_30', 'SUBSTANDARD_PAR_60', 'DOUBTFUL_PAR_90', 'LOSS_PAR_90_PLUS'
    provision_rate_pct DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    provision_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    last_par_evaluation_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.repayment_schedules (
    schedule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    installment_no INT NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(15,2) NOT NULL,
    interest_due DECIMAL(15,2) NOT NULL,
    total_due DECIMAL(15,2) NOT NULL,
    amount_paid DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (account_id) REFERENCES {schema}.loan_accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT uq_rs_installment_{schema} UNIQUE (account_id, installment_no)
);

CREATE TABLE IF NOT EXISTS {schema}.loan_repayments (
    repayment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    transaction_ref VARCHAR(100) NOT NULL UNIQUE,
    amount_paid DECIMAL(15,2) NOT NULL,
    principal_portion DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    interest_portion DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    penalty_portion DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payment_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    payment_channel VARCHAR(50) NOT NULL DEFAULT 'SAVINGS_ACCOUNT',
    remarks TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (account_id) REFERENCES {schema}.loan_accounts(account_id) ON DELETE CASCADE
);

-- =========================================================================
-- Day 2: Portfolio at Risk (PAR) & Regulatory Delinquency Snapshots
-- =========================================================================
CREATE TABLE IF NOT EXISTS {schema}.loan_delinquency_snapshots (
    snapshot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    business_date DATE NOT NULL,
    days_past_due INT NOT NULL DEFAULT 0,
    overdue_principal DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    overdue_interest DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_overdue DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    par_bucket VARCHAR(30) NOT NULL DEFAULT 'CURRENT', -- 'CURRENT', 'WATCHLIST_PAR_30', 'SUBSTANDARD_PAR_60', 'DOUBTFUL_PAR_90', 'LOSS_PAR_90_PLUS'
    provision_rate_pct DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    provision_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (account_id) REFERENCES {schema}.loan_accounts(account_id) ON DELETE CASCADE
);

