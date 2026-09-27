-- 04_transaction.sql: Transaction, Double-Entry Journal & Ledger
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_type') THEN
    CREATE TYPE transaction_type AS ENUM ('CASH_DEPOSIT', 'CASH_WITHDRAWAL', 'MEMBER_TRANSFER', 'SYSTEM_FEE', 'INTEREST_PAYOUT', 'REVERSAL'); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_status') THEN
    CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'entry_type') THEN
    CREATE TYPE entry_type AS ENUM ('DEBIT', 'CREDIT'); END IF; END $$;

CREATE TABLE IF NOT EXISTS {schema}.transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_ref VARCHAR(50) NOT NULL UNIQUE,
    account_no VARCHAR(50) NOT NULL,
    sacco_code VARCHAR(20) NOT NULL,
    user_id UUID NOT NULL,
    processed_by_user_id UUID NOT NULL,
    transaction_type transaction_type NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'ETB',
    status transaction_status NOT NULL DEFAULT 'PENDING',
    narration TEXT,
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.journal_entries (
    entry_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_ref VARCHAR(50) NOT NULL,
    posting_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (transaction_ref) REFERENCES {schema}.transactions(transaction_ref) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.journal_lines (
    line_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id UUID NOT NULL,
    gl_account_code VARCHAR(50) NOT NULL,
    entry_type entry_type NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (entry_id) REFERENCES {schema}.journal_entries(entry_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.transaction_audit_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_ref VARCHAR(50),
    account_no VARCHAR(50),
    performed_by_user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
