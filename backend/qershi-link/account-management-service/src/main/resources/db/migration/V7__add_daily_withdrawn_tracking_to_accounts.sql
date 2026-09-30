-- =========================================================================
-- V7: Add Daily Withdrawal Accumulator to Accounts for Risk Limits Enforcement
-- Tracks cumulative withdrawals per calendar day to enforce daily product limits.
-- =========================================================================

ALTER TABLE accounts
ADD COLUMN IF NOT EXISTS daily_withdrawn_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
ADD COLUMN IF NOT EXISTS daily_withdrawn_date DATE DEFAULT CURRENT_DATE;

CREATE INDEX IF NOT EXISTS idx_acc_daily_withdrawn_date ON accounts(daily_withdrawn_date);
