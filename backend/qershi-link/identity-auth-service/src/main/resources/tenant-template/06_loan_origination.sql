-- 06_loan_origination.sql: Collateral, Groups, Loan Applications & Scoring
CREATE TABLE IF NOT EXISTS {schema}.collateral_types (
    type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_code VARCHAR(50) NOT NULL UNIQUE,
    type_name VARCHAR(100) NOT NULL,
    min_coverage_pct DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.loan_groups (
    group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_name VARCHAR(100) NOT NULL,
    is_formal BOOLEAN NOT NULL DEFAULT FALSE,
    license_no VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.loan_group_members (
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    is_leader BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES {schema}.loan_groups(group_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.loan_applications (
    application_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_no VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    group_id UUID,
    product_id UUID NOT NULL,
    scoring_type VARCHAR(30) NOT NULL,
    amount_requested DECIMAL(15,2) NOT NULL,
    amount_approved DECIMAL(15,2),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (group_id) REFERENCES {schema}.loan_groups(group_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS {schema}.loan_credit_scoring (
    scoring_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL UNIQUE,
    savings_consistency DECIMAL(5,2),
    historical_yield DECIMAL(10,2),
    projected_yield DECIMAL(10,2),
    land_size_hectares DECIMAL(8,2),
    calculated_score DECIMAL(5,2),
    passed_eligibility BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (application_id) REFERENCES {schema}.loan_applications(application_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.loan_collateral (
    collateral_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    estimated_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    document_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (application_id) REFERENCES {schema}.loan_applications(application_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.approval_workflow_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    action_by UUID NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    remarks TEXT,
    action_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (application_id) REFERENCES {schema}.loan_applications(application_id) ON DELETE CASCADE
);
