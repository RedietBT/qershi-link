-- =========================================================================
-- V5: Blind Till Balancing, Banknote Denominations & Closing Logs
-- Tier-1 CBS (Temenos Transact / Finacle) Blind Balancing Standard
-- =========================================================================

-- 1. Ensure till_cash_reconciliations table exists with full variance audit columns
CREATE TABLE IF NOT EXISTS till_cash_reconciliations (
    reconciliation_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    till_id                 UUID NOT NULL,
    teller_user_id          UUID NOT NULL,
    electronic_cash_balance DECIMAL(19,4) NOT NULL,
    physical_cash_counted   DECIMAL(19,4) NOT NULL,
    cash_variance           DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    notes_200_count         INT NOT NULL DEFAULT 0,
    notes_100_count         INT NOT NULL DEFAULT 0,
    notes_50_count          INT NOT NULL DEFAULT 0,
    notes_10_count          INT NOT NULL DEFAULT 0,
    notes_5_count           INT NOT NULL DEFAULT 0,
    coins_amount            DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    variance_type           VARCHAR(20) NOT NULL DEFAULT 'NONE', -- 'NONE', 'SHORTAGE', 'OVERAGE'
    variance_amount         DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    variance_gl_code        VARCHAR(50), -- '5090' (Shortage Expense), '4090' (Overage Income)
    journal_entry_id        UUID,
    status                  VARCHAR(40) NOT NULL DEFAULT 'BALANCED', -- 'BALANCED', 'PENDING_SUPERVISOR_APPROVAL', 'SUPERVISOR_APPROVED'
    supervisor_approved_by  UUID,
    supervisor_approved_at  TIMESTAMPTZ,
    supervisor_notes        TEXT,
    reconciliation_notes    TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tcr_till FOREIGN KEY (till_id) REFERENCES teller_tills(till_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tcr_till_id ON till_cash_reconciliations(till_id);
CREATE INDEX IF NOT EXISTS idx_tcr_teller_user ON till_cash_reconciliations(teller_user_id);
CREATE INDEX IF NOT EXISTS idx_tcr_status ON till_cash_reconciliations(status);

-- 2. Itemized Banknote Denominations Table
CREATE TABLE IF NOT EXISTS till_denominations (
    denomination_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reconciliation_id       UUID NOT NULL,
    denomination_value      DECIMAL(10,2) NOT NULL, -- 200.00, 100.00, 50.00, 10.00, 5.00, 1.00
    quantity                INT NOT NULL DEFAULT 0,
    total_amount            DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_till_denom_rec FOREIGN KEY (reconciliation_id) REFERENCES till_cash_reconciliations(reconciliation_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_till_denom_rec ON till_denominations(reconciliation_id);

-- 3. Historical Till Closing Audit Logs
CREATE TABLE IF NOT EXISTS till_closing_logs (
    log_id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reconciliation_id       UUID NOT NULL,
    till_id                 UUID NOT NULL,
    teller_user_id          UUID NOT NULL,
    closing_mode            VARCHAR(20) NOT NULL DEFAULT 'BLIND', -- 'BLIND', 'ASSISTED'
    electronic_balance      DECIMAL(19,4) NOT NULL,
    physical_total          DECIMAL(19,4) NOT NULL,
    variance                DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    status                  VARCHAR(40) NOT NULL DEFAULT 'BALANCED',
    journal_entry_id        UUID,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tcl_rec FOREIGN KEY (reconciliation_id) REFERENCES till_cash_reconciliations(reconciliation_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tcl_till_id ON till_closing_logs(till_id);
CREATE INDEX IF NOT EXISTS idx_tcl_teller_user ON till_closing_logs(teller_user_id);
