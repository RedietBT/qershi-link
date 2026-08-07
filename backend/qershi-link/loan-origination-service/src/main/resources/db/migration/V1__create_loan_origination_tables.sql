-- =========================================================================
-- V1: Core Loan Origination Service (LOS) Schema DDL
-- =========================================================================

-- 1. Dynamic Collateral Types Configuration Table
CREATE TABLE IF NOT EXISTS collateral_types (
    type_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_code        VARCHAR(50) NOT NULL UNIQUE,
    type_name        VARCHAR(100) NOT NULL,
    min_coverage_pct DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO collateral_types (type_code, type_name, min_coverage_pct) VALUES
    ('LAND',               'Agricultural / Urban Land Title Deed', 120.00),
    ('CROP',               'Standing Crop / Farm Yield Pledge',     100.00),
    ('VEHICLE',            'Motor Vehicle / Farm Machinery',       130.00),
    ('GUARANTOR',          'Personal Member Guarantor Agreement',   100.00),
    ('GOLD',               'Gold & Precious Metals Deposit',        110.00),
    ('SALARY_ASSIGNMENT',  'Employer Payroll Salary Assignment',    100.00)
ON CONFLICT (type_code) DO NOTHING;

-- 2. Borrowing Groups Table
CREATE TABLE IF NOT EXISTS loan_groups (
    group_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_name   VARCHAR(100) NOT NULL,
    is_formal    BOOLEAN NOT NULL DEFAULT FALSE,
    license_no   VARCHAR(50),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_loan_groups_name ON loan_groups(group_name);
CREATE INDEX IF NOT EXISTS idx_loan_groups_license ON loan_groups(license_no);

-- 3. Group Membership Roster Table
CREATE TABLE IF NOT EXISTS loan_group_members (
    group_id   UUID NOT NULL,
    user_id    UUID NOT NULL,
    is_leader  BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_lgm_group FOREIGN KEY (group_id) REFERENCES loan_groups(group_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lgm_user_id ON loan_group_members(user_id);

-- 4. Core Loan Applications Table
CREATE TABLE IF NOT EXISTS loan_applications (
    application_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_no   VARCHAR(50) NOT NULL UNIQUE,
    user_id          UUID NOT NULL,
    group_id         UUID,
    product_id       UUID NOT NULL,
    scoring_type     VARCHAR(50) NOT NULL,
    amount_requested DECIMAL(15,2) NOT NULL,
    amount_approved  DECIMAL(15,2),
    status           VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_la_group FOREIGN KEY (group_id) REFERENCES loan_groups(group_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_la_user_id ON loan_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_la_group_id ON loan_applications(group_id);
CREATE INDEX IF NOT EXISTS idx_la_status ON loan_applications(status);

-- 5. Multi-Factor Credit Scoring Table
CREATE TABLE IF NOT EXISTS loan_credit_scoring (
    scoring_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID NOT NULL UNIQUE,
    savings_consistency DECIMAL(5,2),
    historical_yield    DECIMAL(10,2),
    projected_yield     DECIMAL(10,2),
    land_size_hectares  DECIMAL(8,2),
    calculated_score    DECIMAL(5,2),
    passed_eligibility  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lcs_app FOREIGN KEY (application_id) REFERENCES loan_applications(application_id) ON DELETE CASCADE
);

-- 6. Collateral Assets & Guarantees Table
CREATE TABLE IF NOT EXISTS loan_collateral (
    collateral_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id  UUID NOT NULL,
    type            VARCHAR(50) NOT NULL,
    estimated_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    document_url    TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lc_app FOREIGN KEY (application_id) REFERENCES loan_applications(application_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lc_app_id ON loan_collateral(application_id);

-- 7. Maker-Checker Approval Workflow Audit Logs
CREATE TABLE IF NOT EXISTS approval_workflow_logs (
    log_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    action_by      UUID NOT NULL,
    action_type    VARCHAR(30) NOT NULL,
    remarks        TEXT,
    action_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_awl_app FOREIGN KEY (application_id) REFERENCES loan_applications(application_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_awl_app_id ON approval_workflow_logs(application_id);
CREATE INDEX IF NOT EXISTS idx_awl_action_by ON approval_workflow_logs(action_by);
