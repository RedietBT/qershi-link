-- =========================================================================
-- V3: IFRS 9 / NBE Regulatory Loan Loss Provisioning Schema
-- NBE Directive & IFRS 9 ECL (Expected Credit Loss) Stage Classification
-- GL 5030: Loan Impairment Loss Expense (DEBIT)
-- GL 1039: Allowance for Credit Losses — Contra-Asset (CREDIT)
-- =========================================================================

-- 1. IFRS 9 Portfolio Impairment Provision Runs (one record per month-end run)
CREATE TABLE IF NOT EXISTS loan_impairment_provision_runs (
    run_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_date       DATE NOT NULL,
    run_type            VARCHAR(20) NOT NULL DEFAULT 'MONTH_END', -- MONTH_END, MANUAL
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- PENDING, COMPLETED, FAILED

    -- Portfolio totals
    total_loans_evaluated   INT NOT NULL DEFAULT 0,
    total_portfolio_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,

    -- Bucket totals
    pass_balance            DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    special_mention_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    substandard_balance     DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    doubtful_balance        DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loss_balance            DECIMAL(18,2) NOT NULL DEFAULT 0.00,

    -- Required provision totals
    pass_provision          DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    special_mention_provision DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    substandard_provision   DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    doubtful_provision      DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loss_provision          DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_provision_required DECIMAL(18,2) NOT NULL DEFAULT 0.00,

    -- GL posting reference
    gl_debit_account        VARCHAR(20) NOT NULL DEFAULT '5030',  -- Loan Impairment Loss Expense
    gl_credit_account       VARCHAR(20) NOT NULL DEFAULT '1039',  -- Allowance for Credit Losses
    gl_posting_ref          VARCHAR(100),                          -- Unique journal entry reference
    gl_posted_at            TIMESTAMPTZ,

    -- Audit
    triggered_by            VARCHAR(100) NOT NULL DEFAULT 'SYSTEM_EOD',
    triggered_by_user_id    UUID,
    error_message           TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_llpr_business_date ON loan_impairment_provision_runs(business_date);
CREATE INDEX IF NOT EXISTS idx_llpr_status ON loan_impairment_provision_runs(status);

-- 2. Per-loan IFRS 9 provisioning detail lines (loan-level ECL records)
CREATE TABLE IF NOT EXISTS loan_impairment_provision_lines (
    line_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id              UUID NOT NULL,
    account_id          UUID NOT NULL,
    account_no          VARCHAR(50),
    days_past_due       INT NOT NULL DEFAULT 0,

    -- NBE / IFRS 9 risk classification
    ifrs9_stage         VARCHAR(20) NOT NULL,  -- PASS, SPECIAL_MENTION, SUBSTANDARD, DOUBTFUL, LOSS
    ifrs9_bucket_label  VARCHAR(50) NOT NULL,  -- human-readable label
    dpd_from            INT NOT NULL,
    dpd_to              INT,                   -- NULL means open-ended (360+)

    -- Financial amounts
    outstanding_principal DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    provision_rate_pct    DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    provision_amount      DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_llpl_run FOREIGN KEY (run_id) REFERENCES loan_impairment_provision_runs(run_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_llpl_run_id ON loan_impairment_provision_lines(run_id);
CREATE INDEX IF NOT EXISTS idx_llpl_account_id ON loan_impairment_provision_lines(account_id);
CREATE INDEX IF NOT EXISTS idx_llpl_stage ON loan_impairment_provision_lines(ifrs9_stage);

COMMENT ON TABLE loan_impairment_provision_runs IS 'IFRS 9 month-end ECL impairment calculation runs with GL posting references';
COMMENT ON TABLE loan_impairment_provision_lines IS 'Per-loan IFRS 9 stage classification and provision amounts for each run';
