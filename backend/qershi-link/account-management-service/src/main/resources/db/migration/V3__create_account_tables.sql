-- =========================================================================
-- V3: Core Account Management Tables (Dynamic Product Catalog, Ledger, Liens)
-- =========================================================================

-- 1. Dynamic Product Catalog Engine (FLEXCUBE / Temenos Product Factory)
CREATE TABLE account_products (
    product_id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_code                 VARCHAR(10) NOT NULL UNIQUE,
    product_name                 VARCHAR(150) NOT NULL,
    category                     VARCHAR(50) NOT NULL,
    currency                     VARCHAR(3) NOT NULL DEFAULT 'ETB',
    interest_rate_pa             DECIMAL(7,4) NOT NULL DEFAULT 0.0000,
    posting_frequency            interest_posting_frequency NOT NULL DEFAULT 'MONTHLY',
    min_operating_balance        DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    min_monthly_contribution     DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    term_period_months           INT,
    early_withdrawal_penalty_pct DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active                    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ap_product_code ON account_products(product_code);
CREATE INDEX idx_ap_category ON account_products(category);

-- 2. Core Member Account Ledger Table (FLEXCUBE / Temenos Standard)
CREATE TABLE accounts (
    account_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no          VARCHAR(50) NOT NULL UNIQUE,
    user_id             UUID NOT NULL,
    sacco_code          VARCHAR(20) NOT NULL,
    branch_code         VARCHAR(20) NOT NULL,
    product_code        VARCHAR(10) NOT NULL,
    book_balance        DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    lien_hold_amount    DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    status              account_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    freeze_status       freeze_status NOT NULL DEFAULT 'NONE',
    opened_date         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approved_by_user_id UUID,
    approval_date       TIMESTAMPTZ,
    closed_date         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_acc_product_code FOREIGN KEY (product_code)
        REFERENCES account_products(product_code) ON UPDATE CASCADE
);

CREATE INDEX idx_acc_account_no ON accounts(account_no);
CREATE INDEX idx_acc_user_id ON accounts(user_id);
CREATE INDEX idx_acc_sacco_branch ON accounts(sacco_code, branch_code);
CREATE INDEX idx_acc_status ON accounts(status);

-- 3. Monetary Lien Holds Table (Collateral Blocks & Partial Guarantees)
CREATE TABLE account_liens (
    lien_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no          VARCHAR(50) NOT NULL,
    lien_amount         DECIMAL(19,4) NOT NULL,
    reason              TEXT NOT NULL,
    reference_no        VARCHAR(100),
    placed_by_user_id   UUID NOT NULL,
    released_by_user_id UUID,
    status              lien_status NOT NULL DEFAULT 'ACTIVE',
    placed_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at         TIMESTAMPTZ,

    CONSTRAINT fk_al_account_no FOREIGN KEY (account_no)
        REFERENCES accounts(account_no) ON DELETE CASCADE
);

CREATE INDEX idx_al_account_no ON account_liens(account_no);
CREATE INDEX idx_al_status ON account_liens(status);

-- 4. Governance & Audit Log Trail
CREATE TABLE account_audit_logs (
    log_id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_no           VARCHAR(50),
    user_id              UUID NOT NULL,
    performed_by_user_id UUID NOT NULL,
    action               VARCHAR(100) NOT NULL,
    field_name           VARCHAR(100),
    old_value            TEXT,
    new_value            TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_aal_account_no ON account_audit_logs(account_no);
CREATE INDEX idx_aal_user_id ON account_audit_logs(user_id);
