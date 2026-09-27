-- 02_profile.sql: Member Profile Domain Tables & ENUMs
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'profile_gender') THEN
    CREATE TYPE profile_gender AS ENUM ('MALE', 'FEMALE', 'OTHER'); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'profile_marital_status') THEN
    CREATE TYPE profile_marital_status AS ENUM ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED'); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'profile_member_status') THEN
    CREATE TYPE profile_member_status AS ENUM ('PENDING_APPROVAL', 'ACTIVE', 'SUSPENDED', 'DECEASED', 'CLOSED'); END IF; END $$;

CREATE TABLE IF NOT EXISTS {schema}.member_profiles (
    user_id UUID PRIMARY KEY,
    member_no VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender profile_gender NOT NULL,
    date_of_birth DATE NOT NULL,
    marital_status profile_marital_status NOT NULL DEFAULT 'SINGLE',
    status profile_member_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.member_addresses (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    primary_phone VARCHAR(15) NOT NULL UNIQUE,
    secondary_phone VARCHAR(15),
    email VARCHAR(255),
    region VARCHAR(100) NOT NULL,
    zone_subcity VARCHAR(100) NOT NULL,
    woreda VARCHAR(100) NOT NULL,
    house_number VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES {schema}.member_profiles(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.member_employments (
    employment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    occupation_sector VARCHAR(100) NOT NULL,
    employer_name VARCHAR(200),
    monthly_income DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    tin_number VARCHAR(30),
    employee_id VARCHAR(50),
    external_employee_id VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES {schema}.member_profiles(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.member_governance (
    governance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    submitted_by_user_id UUID NOT NULL,
    approved_by_user_id UUID,
    approval_date TIMESTAMPTZ,
    remarks TEXT,
    FOREIGN KEY (user_id) REFERENCES {schema}.member_profiles(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.member_identifications (
    identification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    id_type VARCHAR(50) NOT NULL,
    id_number VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    issuing_authority VARCHAR(150) NOT NULL,
    kyc_status VARCHAR(50) NOT NULL DEFAULT 'UNVERIFIED',
    verified_by_user_id UUID,
    verification_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES {schema}.member_profiles(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.next_of_kin (
    kin_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    relationship VARCHAR(100) NOT NULL,
    primary_phone VARCHAR(15) NOT NULL,
    id_number VARCHAR(100),
    physical_address VARCHAR(255),
    allocation_percentage DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES {schema}.member_profiles(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.profile_documents (
    document_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES {schema}.member_profiles(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS {schema}.profile_audit_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    performed_by_user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES {schema}.member_profiles(user_id) ON DELETE CASCADE
);
