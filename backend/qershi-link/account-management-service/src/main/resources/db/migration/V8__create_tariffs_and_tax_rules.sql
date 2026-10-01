-- =========================================================================
-- V8: Core Banking Fee & Tariff Engine & Statutory Tax Records
-- Configurable transaction tariffs (flat & percentage) and interest withholding tax tracking
-- =========================================================================

CREATE TABLE IF NOT EXISTS tariffs (
    tariff_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tariff_code         VARCHAR(50) NOT NULL UNIQUE,
    tariff_name         VARCHAR(150) NOT NULL,
    transaction_type    VARCHAR(50) NOT NULL, -- 'WITHDRAWAL', 'TRANSFER_INTERNAL', 'TRANSFER_EXTERNAL', 'STATEMENT_PRINT', 'LOAN_PROCESSING'
    fee_type            VARCHAR(20) NOT NULL DEFAULT 'FLAT', -- 'FLAT', 'PERCENTAGE'
    fee_value           DECIMAL(15,4) NOT NULL DEFAULT 0.0000,
    min_fee             DECIMAL(15,2),
    max_fee             DECIMAL(15,2),
    fee_gl_code         VARCHAR(50) NOT NULL DEFAULT '4020',
    currency            VARCHAR(3) NOT NULL DEFAULT 'ETB',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    description         TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tariffs_txn_type ON tariffs(transaction_type);
CREATE INDEX IF NOT EXISTS idx_tariffs_is_active ON tariffs(is_active);

-- Seed Standard Cooperative Banking Tariffs
INSERT INTO tariffs (tariff_code, tariff_name, transaction_type, fee_type, fee_value, min_fee, max_fee, fee_gl_code, description)
VALUES
('TAR-WTH-01', 'Over-The-Counter Cash Withdrawal Fee', 'WITHDRAWAL', 'FLAT', 10.0000, 10.00, 10.00, '4020', 'Standard flat service fee for teller cash withdrawals'),
('TAR-TRF-INT', 'Internal Member-to-Member Transfer Fee', 'TRANSFER_INTERNAL', 'PERCENTAGE', 0.2500, 2.00, 25.00, '4020', '0.25% fee on internal account transfers (min 2 ETB, max 25 ETB)'),
('TAR-TRF-EXT', 'External Inter-Bank Transfer Fee', 'TRANSFER_EXTERNAL', 'FLAT', 25.0000, 25.00, 25.00, '4020', 'RTGS / ACH external clearing commission'),
('TAR-STMT-01', 'Physical Statement Print Fee', 'STATEMENT_PRINT', 'FLAT', 15.0000, 15.00, 15.00, '4020', 'Paper statement printing surcharge'),
('TAR-LOAN-APP', 'Loan Processing & Appraisal Fee', 'LOAN_PROCESSING', 'PERCENTAGE', 1.0000, 100.00, 5000.00, '4021', '1.0% origination appraisal charge on approved loans')
ON CONFLICT (tariff_code) DO NOTHING;

-- Table to record monthly interest withholding tax deductions
CREATE TABLE IF NOT EXISTS interest_tax_deduction_logs (
    log_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no          VARCHAR(50) NOT NULL,
    business_date       DATE NOT NULL,
    gross_interest      DECIMAL(19,4) NOT NULL,
    tax_rate_pct        DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    tax_withheld        DECIMAL(19,4) NOT NULL,
    net_interest        DECIMAL(19,4) NOT NULL,
    wht_gl_code         VARCHAR(50) NOT NULL DEFAULT '2091',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_itdl_account_no ON interest_tax_deduction_logs(account_no);
CREATE INDEX IF NOT EXISTS idx_itdl_business_date ON interest_tax_deduction_logs(business_date);
