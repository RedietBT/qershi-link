-- =========================================================================
-- V6: SACCO Maker-Checker & Policy Rules Engine
-- Stores Four-Eyes workflow toggles, transaction limits, anti-self-approval enforcement,
-- domain Maker/Checker role clearance assignments, and per-product rules.
-- =========================================================================

CREATE TABLE IF NOT EXISTS sacco_maker_checker_rules (
    rule_id                             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sacco_code                          VARCHAR(20) NOT NULL DEFAULT '0001',
    enable_member_onboarding_checker    BOOLEAN NOT NULL DEFAULT true,
    enable_account_opening_checker      BOOLEAN NOT NULL DEFAULT true,
    enable_account_freeze_checker       BOOLEAN NOT NULL DEFAULT true,
    enable_loan_approval_checker        BOOLEAN NOT NULL DEFAULT true,
    enable_loan_disbursement_checker    BOOLEAN NOT NULL DEFAULT true,
    transaction_checker_threshold       NUMERIC(18, 4) NOT NULL DEFAULT 50000.0000,
    daily_account_limit_threshold       NUMERIC(18, 4) NOT NULL DEFAULT 200000.0000,
    enforce_anti_self_approval          BOOLEAN NOT NULL DEFAULT true,
    member_maker_roles                  VARCHAR(255) NOT NULL DEFAULT 'TELLER,CUSTOMER_SERVICE,ADMIN',
    member_checker_roles                VARCHAR(255) NOT NULL DEFAULT 'BRANCH_MANAGER,SACCO_ADMIN,AUDITOR',
    account_maker_roles                 VARCHAR(255) NOT NULL DEFAULT 'TELLER,CUSTOMER_SERVICE,ADMIN',
    account_checker_roles               VARCHAR(255) NOT NULL DEFAULT 'BRANCH_MANAGER,SACCO_ADMIN',
    loan_maker_roles                    VARCHAR(255) NOT NULL DEFAULT 'LOAN_OFFICER,ADMIN',
    loan_checker_roles                  VARCHAR(255) NOT NULL DEFAULT 'BRANCH_MANAGER,SACCO_ADMIN',
    created_at                          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mcr_sacco_code ON sacco_maker_checker_rules(sacco_code);

-- Seed initial rule default
INSERT INTO sacco_maker_checker_rules (
    sacco_code,
    enable_member_onboarding_checker,
    enable_account_opening_checker,
    enable_account_freeze_checker,
    enable_loan_approval_checker,
    enable_loan_disbursement_checker,
    transaction_checker_threshold,
    daily_account_limit_threshold,
    enforce_anti_self_approval,
    member_maker_roles,
    member_checker_roles,
    account_maker_roles,
    account_checker_roles,
    loan_maker_roles,
    loan_checker_roles
) VALUES (
    '0001',
    true,
    true,
    true,
    true,
    true,
    50000.0000,
    200000.0000,
    true,
    'TELLER,CUSTOMER_SERVICE,ADMIN',
    'BRANCH_MANAGER,SACCO_ADMIN,AUDITOR',
    'TELLER,CUSTOMER_SERVICE,ADMIN',
    'BRANCH_MANAGER,SACCO_ADMIN',
    'LOAN_OFFICER,ADMIN',
    'BRANCH_MANAGER,SACCO_ADMIN'
) ON CONFLICT DO NOTHING;

-- Table for per-account-product Maker-Checker rules and risk limits
CREATE TABLE IF NOT EXISTS product_maker_checker_rules (
    rule_id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_code            VARCHAR(20) NOT NULL UNIQUE,
    product_name            VARCHAR(150) NOT NULL,
    category                VARCHAR(50),
    min_operating_balance   NUMERIC(18, 4) NOT NULL DEFAULT 100.0000,
    max_balance_limit       NUMERIC(18, 4) NOT NULL DEFAULT 1000000.0000,
    single_withdrawal_limit NUMERIC(18, 4) NOT NULL DEFAULT 50000.0000,
    daily_withdrawal_limit  NUMERIC(18, 4) NOT NULL DEFAULT 150000.0000,
    enable_maker_checker    BOOLEAN NOT NULL DEFAULT true,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pmcr_code ON product_maker_checker_rules(product_code);

-- Seed default product rules for standard products
INSERT INTO product_maker_checker_rules (product_code, product_name, category, min_operating_balance, max_balance_limit, single_withdrawal_limit, daily_withdrawal_limit, enable_maker_checker)
VALUES
    ('101', 'Regular Voluntary Savings', 'SAVINGS', 100.0000, 2000000.0000, 50000.0000, 150000.0000, true),
    ('102', 'Fixed Term Deposit', 'TERM_DEPOSIT', 5000.0000, 10000000.0000, 100000.0000, 500000.0000, true),
    ('103', 'Compulsory Member Savings', 'COMPULSORY', 200.0000, 5000000.0000, 25000.0000, 100000.0000, true),
    ('104', 'Youth & Student Savings', 'SAVINGS', 50.0000, 500000.0000, 10000.0000, 30000.0000, true)
ON CONFLICT (product_code) DO NOTHING;
