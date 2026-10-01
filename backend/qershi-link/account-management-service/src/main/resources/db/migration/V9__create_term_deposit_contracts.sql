-- =========================================================================
-- V9: Gap 5 — Fixed Term Deposit (FD) Contracts & Early Penalty Break
-- Temenos Transact / Finacle Tier-1 CBS Standard
-- =========================================================================

-- 1. Term Deposit Contract Status Enum
CREATE TYPE term_deposit_status AS ENUM (
    'ACTIVE',          -- Funds locked, earning FD interest
    'MATURED',         -- Reached maturity date, awaiting rollover or payout
    'ROLLED_OVER',     -- Automatically re-invested into a new term
    'CLOSED_NORMAL',   -- Closed at maturity — full principal + interest credited
    'CLOSED_EARLY',    -- Broken before maturity — penalty applied
    'PENDING_APPROVAL' -- Awaiting Maker-Checker authorization
);

-- 2. Core Term Deposit Contracts Table
-- One row per member FD contract (can have multiple active at once)
CREATE TABLE term_deposit_contracts (
    contract_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_no             VARCHAR(50) NOT NULL UNIQUE,   -- e.g. FD-20261001-001
    account_no              VARCHAR(50) NOT NULL,          -- source savings account (FK to accounts)
    user_id                 UUID NOT NULL,
    sacco_code              VARCHAR(20) NOT NULL,
    branch_code             VARCHAR(20) NOT NULL,

    -- Principal & Terms
    principal_amount        DECIMAL(19,4) NOT NULL,
    tenor_months            INT NOT NULL,                  -- 6, 12, 24, 36 months
    agreed_interest_rate_pa DECIMAL(7,4) NOT NULL,         -- e.g. 9.50 for 9.5% p.a.
    early_break_penalty_pct DECIMAL(5,2) NOT NULL DEFAULT 2.00, -- e.g. 2.00 for 2%

    -- Dates
    start_date              DATE NOT NULL DEFAULT CURRENT_DATE,
    maturity_date           DATE NOT NULL,                 -- start_date + tenor_months
    closed_date             DATE,

    -- Accrued Interest Tracking
    accrued_interest        DECIMAL(19,4) NOT NULL DEFAULT 0.0000,  -- running daily accrual
    capitalized_interest    DECIMAL(19,4) NOT NULL DEFAULT 0.0000,  -- interest paid at closure

    -- Status
    status                  term_deposit_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    auto_rollover           BOOLEAN NOT NULL DEFAULT FALSE,  -- auto re-invest at maturity
    rollover_tenor_months   INT,                             -- if NULL, reuse tenor_months

    -- Penalty Break (populated only if CLOSED_EARLY)
    penalty_amount          DECIMAL(19,4),
    net_payout_amount       DECIMAL(19,4),               -- principal + interest - penalty

    -- GL Posting references (double-entry audit)
    gl_debit_account        VARCHAR(20) NOT NULL DEFAULT '2060',  -- Term Deposit Liability
    gl_credit_account       VARCHAR(20) NOT NULL DEFAULT '1010',  -- Cash / Savings Account
    opening_gl_ref          VARCHAR(100),  -- journal ref when FD opened
    closing_gl_ref          VARCHAR(100),  -- journal ref when FD closed/matured

    -- Maker-Checker
    maker_user_id           UUID NOT NULL,
    maker_notes             TEXT,
    checker_user_id         UUID,
    checker_notes           TEXT,
    approved_at             TIMESTAMPTZ,

    -- Audit
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_tdc_account FOREIGN KEY (account_no)
        REFERENCES accounts(account_no) ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tdc_account_no    ON term_deposit_contracts(account_no);
CREATE INDEX IF NOT EXISTS idx_tdc_user_id       ON term_deposit_contracts(user_id);
CREATE INDEX IF NOT EXISTS idx_tdc_status        ON term_deposit_contracts(status);
CREATE INDEX IF NOT EXISTS idx_tdc_maturity_date ON term_deposit_contracts(maturity_date);
CREATE INDEX IF NOT EXISTS idx_tdc_sacco_code    ON term_deposit_contracts(sacco_code);

-- 3. Term Deposit Daily Accrual Log (audit trail for each EOD interest posting)
CREATE TABLE term_deposit_accrual_logs (
    log_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id         UUID NOT NULL,
    business_date       DATE NOT NULL,
    daily_accrual       DECIMAL(19,4) NOT NULL,
    running_total       DECIMAL(19,4) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tdal_contract FOREIGN KEY (contract_id)
        REFERENCES term_deposit_contracts(contract_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tdal_contract_id  ON term_deposit_accrual_logs(contract_id);
CREATE INDEX IF NOT EXISTS idx_tdal_business_date ON term_deposit_accrual_logs(business_date);

COMMENT ON TABLE term_deposit_contracts IS 'Fixed Term Deposit (FD) contracts with Temenos/Finacle penalty-break engine';
COMMENT ON TABLE term_deposit_accrual_logs IS 'Daily EOD interest accrual audit log per FD contract';
