-- =========================================================================
-- V1: Core Loan Management Service (LMS) Schema DDL
-- =========================================================================

-- 1. Dynamic Loan Penalty Policies Configuration Table
CREATE TABLE IF NOT EXISTS loan_penalty_configs (
    config_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_code       VARCHAR(50) NOT NULL UNIQUE,
    policy_name       VARCHAR(100) NOT NULL,
    grace_period_days INT NOT NULL DEFAULT 5,
    penalty_rate_pct  DECIMAL(5,2) NOT NULL DEFAULT 2.00,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Disbursed Loan Accounts Table
CREATE TABLE IF NOT EXISTS loan_accounts (
    account_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no        VARCHAR(50) NOT NULL UNIQUE,
    application_id    UUID NOT NULL UNIQUE,
    user_id           UUID NOT NULL,
    product_id        UUID NOT NULL,
    principal_amount  DECIMAL(15,2) NOT NULL,
    interest_rate_pct DECIMAL(5,2) NOT NULL,
    term_months       INT NOT NULL,
    interest_type     VARCHAR(30) NOT NULL DEFAULT 'REDUCING_BALANCE',
    disbursement_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status            VARCHAR(30) NOT NULL DEFAULT 'DISBURSED',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_la_user_id ON loan_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_la_status ON loan_accounts(status);
CREATE INDEX IF NOT EXISTS idx_la_app_id ON loan_accounts(application_id);

-- 3. Amortization Repayment Schedules Table
CREATE TABLE IF NOT EXISTS repayment_schedules (
    schedule_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id     UUID NOT NULL,
    installment_no INT NOT NULL,
    due_date       DATE NOT NULL,
    principal_due  DECIMAL(15,2) NOT NULL,
    interest_due   DECIMAL(15,2) NOT NULL,
    total_due      DECIMAL(15,2) NOT NULL,
    amount_paid    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status         VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_rs_account FOREIGN KEY (account_id) REFERENCES loan_accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT uq_rs_installment UNIQUE (account_id, installment_no)
);

CREATE INDEX IF NOT EXISTS idx_rs_account_id ON repayment_schedules(account_id);
CREATE INDEX IF NOT EXISTS idx_rs_due_date ON repayment_schedules(due_date);
CREATE INDEX IF NOT EXISTS idx_rs_status ON repayment_schedules(status);

-- 4. Loan Repayment Transactions Audit Table
CREATE TABLE IF NOT EXISTS loan_repayments (
    repayment_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id        UUID NOT NULL,
    transaction_ref   VARCHAR(100) NOT NULL UNIQUE,
    amount_paid       DECIMAL(15,2) NOT NULL,
    principal_portion DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    interest_portion  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    penalty_portion   DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payment_date      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    payment_channel   VARCHAR(50) NOT NULL DEFAULT 'SAVINGS_ACCOUNT',
    remarks           TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lr_account FOREIGN KEY (account_id) REFERENCES loan_accounts(account_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lr_account_id ON loan_repayments(account_id);
CREATE INDEX IF NOT EXISTS idx_lr_tx_ref ON loan_repayments(transaction_ref);
