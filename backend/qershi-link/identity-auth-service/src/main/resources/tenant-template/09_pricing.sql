-- 09_pricing.sql: Pricing, Fee Tariffs & Statutory Tax Rules for Tenant
CREATE TABLE IF NOT EXISTS {schema}.tariffs (
    tariff_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tariff_code VARCHAR(30) NOT NULL UNIQUE,
    tariff_name VARCHAR(100) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    fee_type VARCHAR(20) NOT NULL,
    fee_value NUMERIC(15,4) NOT NULL,
    min_fee NUMERIC(15,4),
    max_fee NUMERIC(15,4),
    fee_gl_code VARCHAR(50) NOT NULL DEFAULT '4020',
    currency VARCHAR(3) NOT NULL DEFAULT 'ETB',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tenant_tariffs_type ON {schema}.tariffs(transaction_type, is_active);

CREATE TABLE IF NOT EXISTS {schema}.interest_tax_deduction_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no VARCHAR(50) NOT NULL,
    business_date DATE NOT NULL,
    gross_interest NUMERIC(15,4) NOT NULL,
    tax_rate_pct NUMERIC(5,2) NOT NULL DEFAULT 5.00,
    tax_withheld NUMERIC(15,4) NOT NULL,
    net_interest NUMERIC(15,4) NOT NULL,
    wht_gl_code VARCHAR(50) NOT NULL DEFAULT '2091',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tenant_wht_acc_date ON {schema}.interest_tax_deduction_logs(account_no, business_date);

-- Seed standard enterprise cooperative default tariffs for new tenant
INSERT INTO {schema}.tariffs (tariff_code, tariff_name, transaction_type, fee_type, fee_value, min_fee, max_fee, fee_gl_code, description)
VALUES
('TAR-WTH-01', 'Over-The-Counter Cash Withdrawal Fee', 'WITHDRAWAL', 'FLAT', 10.0000, 10.0000, 10.0000, '4020', 'Standard service fee for teller cash withdrawals'),
('TAR-TRF-01', 'Internal Member-to-Member Transfer Fee', 'TRANSFER_INTERNAL', 'FLAT', 5.0000, 5.0000, 5.0000, '4020', 'Nominal transfer processing fee for intra-cooperative movement'),
('PRC-LOAN-001', 'Loan Processing & Appraisal Fee', 'LOAN_PROCESSING', 'PERCENTAGE', 1.0000, 100.0000, 5000.0000, '4021', '1.0% origination appraisal charge on approved loans'),
('TAR-STMT-01', 'Physical Account Statement Print Fee', 'STATEMENT_PRINT', 'FLAT', 25.0000, 25.0000, 25.0000, '4020', 'Charge per physical printed account statement')
ON CONFLICT (tariff_code) DO NOTHING;
