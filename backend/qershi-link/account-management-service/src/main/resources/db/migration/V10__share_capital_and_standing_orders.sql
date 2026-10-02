-- =========================================================================
-- V10: Day 5 — Share Capital, Dividends & Standing Orders Engine
-- Temenos Transact / Finacle / Apache Fineract SACCO Tier-1 CBS Standard
-- =========================================================================

-- 1. Sequence for Share Certificate Serial Numbers
CREATE SEQUENCE IF NOT EXISTS share_certificate_serial_seq START WITH 100001 INCREMENT BY 1;

-- 2. Share Accounts Table (Member Equity Account under GL 3100)
CREATE TABLE IF NOT EXISTS share_accounts (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id            UUID NOT NULL,
    account_number       VARCHAR(32) NOT NULL UNIQUE,
    total_shares         INTEGER NOT NULL DEFAULT 0 CHECK (total_shares >= 0),
    share_nominal_value  NUMERIC(19,4) NOT NULL DEFAULT 1000.0000 CHECK (share_nominal_value > 0),
    total_amount         NUMERIC(19,4) NOT NULL DEFAULT 0.0000 CHECK (total_amount >= 0),
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    sacco_code           VARCHAR(20),
    branch_code          VARCHAR(20),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_share_acc_member ON share_accounts(member_id);
CREATE INDEX IF NOT EXISTS idx_share_acc_status ON share_accounts(status);

-- 3. Share Certificates Table (Tracks Legal Certificates & Serial Numbers)
CREATE TABLE IF NOT EXISTS share_certificates (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    share_account_id     UUID NOT NULL REFERENCES share_accounts(id) ON DELETE CASCADE,
    certificate_number   VARCHAR(50) NOT NULL UNIQUE,
    start_serial         BIGINT NOT NULL,
    end_serial           BIGINT NOT NULL,
    share_count          INTEGER NOT NULL CHECK (share_count > 0),
    issue_date           DATE NOT NULL DEFAULT CURRENT_DATE,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, TRANSFERRED, SURRENDERED
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_serial_range CHECK (end_serial >= start_serial)
);

CREATE INDEX IF NOT EXISTS idx_share_cert_account ON share_certificates(share_account_id);
CREATE INDEX IF NOT EXISTS idx_share_cert_status ON share_certificates(status);

-- 4. Share Transfers Table (Peer-to-Peer Transfer Audit & Maker-Checker)
CREATE TABLE IF NOT EXISTS share_transfers (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_member_id       UUID NOT NULL,
    to_member_id         UUID NOT NULL,
    certificate_id       UUID NOT NULL REFERENCES share_certificates(id),
    share_count          INTEGER NOT NULL CHECK (share_count > 0),
    transfer_price       NUMERIC(19,4) NOT NULL CHECK (transfer_price >= 0),
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL', -- PENDING_APPROVAL, APPROVED, REJECTED
    approved_by          UUID,
    approved_at          TIMESTAMPTZ,
    rejection_reason     TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_share_trf_from ON share_transfers(from_member_id);
CREATE INDEX IF NOT EXISTS idx_share_trf_to ON share_transfers(to_member_id);
CREATE INDEX IF NOT EXISTS idx_share_trf_status ON share_transfers(status);

-- 5. Annual AGM Dividend Distributions Header Table
CREATE TABLE IF NOT EXISTS dividend_distributions (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fiscal_year                 INTEGER NOT NULL UNIQUE,
    net_profit_pool             NUMERIC(19,4) NOT NULL DEFAULT 0.0000 CHECK (net_profit_pool >= 0),
    declared_rate_percent       NUMERIC(7,4) NOT NULL DEFAULT 0.0000 CHECK (declared_rate_percent >= 0),
    total_dividend_distributed  NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    total_tax_withheld          NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    qualified_members_count     INTEGER NOT NULL DEFAULT 0,
    status                      VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, SIMULATED, POSTED
    simulated_at                TIMESTAMPTZ,
    posted_at                   TIMESTAMPTZ,
    posted_by                   UUID,
    gl_journal_ref              VARCHAR(100),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Dividend Allocations Table (Itemized Member Distribution Ledger)
CREATE TABLE IF NOT EXISTS dividend_allocations (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    distribution_id             UUID NOT NULL REFERENCES dividend_distributions(id) ON DELETE CASCADE,
    member_id                   UUID NOT NULL,
    share_account_id            UUID NOT NULL REFERENCES share_accounts(id),
    destination_account_id      UUID,
    destination_account_number  VARCHAR(50),
    weighted_average_shares     NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    gross_dividend              NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    tax_withheld                NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    net_dividend_payout         NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, POSTED, FAILED
    failure_reason              TEXT,
    gl_journal_ref              VARCHAR(100),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_div_alloc_dist ON dividend_allocations(distribution_id);
CREATE INDEX IF NOT EXISTS idx_div_alloc_member ON dividend_allocations(member_id);
CREATE INDEX IF NOT EXISTS idx_div_alloc_status ON dividend_allocations(status);

-- 7. Standing Orders Table (Automated Recurring Sweeps)
CREATE TABLE IF NOT EXISTS standing_orders (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    standing_order_no           VARCHAR(50) NOT NULL UNIQUE,
    source_account_id           UUID NOT NULL,
    source_account_no           VARCHAR(50) NOT NULL,
    target_account_id           UUID NOT NULL,
    target_account_no           VARCHAR(50) NOT NULL,
    member_id                   UUID NOT NULL,
    amount                      NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    frequency                   VARCHAR(20) NOT NULL DEFAULT 'MONTHLY', -- DAILY, WEEKLY, BI_WEEKLY, MONTHLY
    day_of_month                INTEGER CHECK (day_of_month BETWEEN 1 AND 31),
    day_of_week                 VARCHAR(15),
    start_date                  DATE NOT NULL DEFAULT CURRENT_DATE,
    next_run_date               DATE NOT NULL,
    end_date                    DATE,
    total_executions_count      INTEGER NOT NULL DEFAULT 0,
    failed_attempts_count       INTEGER NOT NULL DEFAULT 0,
    description                 VARCHAR(255),
    status                      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, COMPLETED, FAILED, CANCELLED
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sto_next_run ON standing_orders(next_run_date, status);
CREATE INDEX IF NOT EXISTS idx_sto_source ON standing_orders(source_account_id);
CREATE INDEX IF NOT EXISTS idx_sto_member ON standing_orders(member_id);

-- 8. Standing Order Executions Table (Audit & Execution History)
CREATE TABLE IF NOT EXISTS standing_order_executions (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    standing_order_id           UUID NOT NULL REFERENCES standing_orders(id) ON DELETE CASCADE,
    execution_date              DATE NOT NULL DEFAULT CURRENT_DATE,
    amount                      NUMERIC(19,4) NOT NULL,
    status                      VARCHAR(25) NOT NULL, -- SUCCESS, INSUFFICIENT_FUNDS, FAILED
    failure_reason              TEXT,
    journal_entry_id            UUID,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sto_exec_order ON standing_order_executions(standing_order_id);
CREATE INDEX IF NOT EXISTS idx_sto_exec_date ON standing_order_executions(execution_date);
