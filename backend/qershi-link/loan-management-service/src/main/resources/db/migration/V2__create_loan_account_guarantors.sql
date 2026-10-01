-- =========================================================================
-- V2: Peer Guarantors & Savings Liens for Loan Accounts (LMS)
-- Tracks active lien holds on guarantor accounts and records their lifecycle.
-- =========================================================================

CREATE TABLE IF NOT EXISTS loan_account_guarantors (
    guarantor_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id          UUID NOT NULL,
    application_id      UUID,
    guarantor_user_id   UUID NOT NULL,
    guarantor_name      VARCHAR(150),
    guarantor_phone     VARCHAR(20),
    savings_account_no  VARCHAR(50) NOT NULL,
    guaranteed_amount   DECIMAL(15,2) NOT NULL,
    lien_id             UUID,
    status              VARCHAR(30) NOT NULL DEFAULT 'HELD', -- 'HELD', 'RELEASED', 'INVOKED'
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lag_account FOREIGN KEY (account_id) REFERENCES loan_accounts(account_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lag_account_id ON loan_account_guarantors(account_id);
CREATE INDEX IF NOT EXISTS idx_lag_app_id ON loan_account_guarantors(application_id);
CREATE INDEX IF NOT EXISTS idx_lag_savings_account ON loan_account_guarantors(savings_account_no);
CREATE INDEX IF NOT EXISTS idx_lag_status ON loan_account_guarantors(status);
