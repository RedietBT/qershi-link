-- =========================================================================
-- V4: Core Transaction Management & Journal Posting Engine Tables
-- =========================================================================

-- 1. ENUM Types Setup for Transaction Domain
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_type') THEN
    CREATE TYPE transaction_type AS ENUM ('CASH_DEPOSIT', 'CASH_WITHDRAWAL', 'MEMBER_TRANSFER', 'SYSTEM_FEE', 'INTEREST_PAYOUT', 'REVERSAL');
END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_status') THEN
    CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED');
END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'entry_type') THEN
    CREATE TYPE entry_type AS ENUM ('DEBIT', 'CREDIT');
END IF; END $$;

-- 2. Core Financial Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_ref      VARCHAR(50) NOT NULL UNIQUE,
    account_no           VARCHAR(50) NOT NULL,
    sacco_code           VARCHAR(20) NOT NULL,
    user_id              UUID NOT NULL,
    processed_by_user_id UUID NOT NULL,
    transaction_type     transaction_type NOT NULL,
    amount               DECIMAL(19,4) NOT NULL,
    currency             VARCHAR(3) NOT NULL DEFAULT 'ETB',
    status               transaction_status NOT NULL DEFAULT 'PENDING',
    narration            TEXT,
    idempotency_key      VARCHAR(100) UNIQUE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tx_account_no ON transactions(account_no);
CREATE INDEX IF NOT EXISTS idx_tx_transaction_ref ON transactions(transaction_ref);
CREATE INDEX IF NOT EXISTS idx_tx_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_tx_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_tx_idempotency ON transactions(idempotency_key);

-- 3. General Ledger (GL) Journal Entries Header Table
CREATE TABLE IF NOT EXISTS journal_entries (
    entry_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_ref VARCHAR(50) NOT NULL,
    posting_date    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    description     VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_je_transaction_ref FOREIGN KEY (transaction_ref)
        REFERENCES transactions(transaction_ref) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_je_transaction_ref ON journal_entries(transaction_ref);
CREATE INDEX IF NOT EXISTS idx_je_posting_date ON journal_entries(posting_date);

-- 4. General Ledger (GL) Double-Entry Bookkeeping Lines Table
CREATE TABLE IF NOT EXISTS journal_lines (
    line_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id        UUID NOT NULL,
    gl_account_code VARCHAR(50) NOT NULL,
    entry_type      entry_type NOT NULL,
    amount          DECIMAL(19,4) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_jl_entry_id FOREIGN KEY (entry_id)
        REFERENCES journal_entries(entry_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_jl_entry_id ON journal_lines(entry_id);
CREATE INDEX IF NOT EXISTS idx_jl_gl_code ON journal_lines(gl_account_code);

-- 5. Transaction Audit & Governance Log
CREATE TABLE IF NOT EXISTS transaction_audit_logs (
    log_id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_ref      VARCHAR(50),
    account_no           VARCHAR(50),
    performed_by_user_id UUID NOT NULL,
    action               VARCHAR(100) NOT NULL,
    details              TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tal_tx_ref ON transaction_audit_logs(transaction_ref);
CREATE INDEX IF NOT EXISTS idx_tal_account_no ON transaction_audit_logs(account_no);
