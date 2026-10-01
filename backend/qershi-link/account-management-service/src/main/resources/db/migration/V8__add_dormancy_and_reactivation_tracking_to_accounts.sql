-- =========================================================================
-- V8: Account Dormancy and Supervisor KYC Reactivation Tracking
-- Tracks dormancy cutoff date and Maker-Checker KYC reactivation lifecycle
-- =========================================================================

ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS dormancy_date DATE,
    ADD COLUMN IF NOT EXISTS reactivation_status VARCHAR(30) NOT NULL DEFAULT 'NONE',
    ADD COLUMN IF NOT EXISTS reactivation_maker_id UUID,
    ADD COLUMN IF NOT EXISTS reactivation_maker_notes TEXT,
    ADD COLUMN IF NOT EXISTS reactivation_checker_id UUID,
    ADD COLUMN IF NOT EXISTS reactivation_checker_notes TEXT,
    ADD COLUMN IF NOT EXISTS reactivated_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_accounts_dormancy ON accounts(status, dormancy_date);
CREATE INDEX IF NOT EXISTS idx_accounts_reactivation ON accounts(reactivation_status);
