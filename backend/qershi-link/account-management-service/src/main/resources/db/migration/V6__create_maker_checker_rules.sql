-- =========================================================================
-- V6: SACCO Maker-Checker & Policy Rules Engine
-- Stores configurable Four-Eyes workflow toggles, transaction threshold limits,
-- and anti-self-approval enforcement settings per SACCO tenant.
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
    enforce_anti_self_approval
) VALUES (
    '0001',
    true,
    true,
    true,
    true,
    true,
    50000.0000,
    200000.0000,
    true
) ON CONFLICT DO NOTHING;
