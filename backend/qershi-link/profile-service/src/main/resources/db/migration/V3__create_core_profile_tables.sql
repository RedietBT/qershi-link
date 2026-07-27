-- =========================================================================
-- V3: Core Member Profile Tables (3NF Normalized Layout)
-- Stores demographic, contact, employment, and governance sign-off records.
-- =========================================================================

-- 1. Core Identity & Demographics
CREATE TABLE member_profiles (
    user_id        UUID PRIMARY KEY,
    member_no      VARCHAR(50) NOT NULL UNIQUE,
    first_name     VARCHAR(100) NOT NULL,
    middle_name    VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    gender         profile_gender NOT NULL,
    date_of_birth  DATE NOT NULL,
    marital_status profile_marital_status NOT NULL DEFAULT 'SINGLE',
    status         profile_member_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mp_member_no ON member_profiles(member_no);
CREATE INDEX idx_mp_status ON member_profiles(status);

-- 2. Contact & Physical Location
CREATE TABLE member_addresses (
    address_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL UNIQUE,
    primary_phone   VARCHAR(15) NOT NULL UNIQUE,
    secondary_phone VARCHAR(15),
    email           VARCHAR(255),
    region          VARCHAR(100) NOT NULL,
    zone_subcity    VARCHAR(100) NOT NULL,
    woreda          VARCHAR(100) NOT NULL,
    house_number    VARCHAR(50),

    CONSTRAINT fk_ma_user FOREIGN KEY (user_id)
        REFERENCES member_profiles(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_ma_phone ON member_addresses(primary_phone);

-- 3. Economic & Financial Profile
CREATE TABLE member_employments (
    employment_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL UNIQUE,
    occupation_sector VARCHAR(100) NOT NULL,
    employer_name     VARCHAR(200),
    monthly_income    DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    tin_number        VARCHAR(30),

    CONSTRAINT fk_me_user FOREIGN KEY (user_id)
        REFERENCES member_profiles(user_id) ON DELETE CASCADE
);

-- 4. Supervisor Onboarding Governance
CREATE TABLE member_governance (
    governance_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL UNIQUE,
    submitted_by_user_id UUID NOT NULL,
    approved_by_user_id  UUID,
    approval_date        TIMESTAMPTZ,
    remarks              TEXT,

    CONSTRAINT fk_mg_user FOREIGN KEY (user_id)
        REFERENCES member_profiles(user_id) ON DELETE CASCADE
);
