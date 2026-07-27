-- =========================================================================
-- V4: KYC Registry & Next-of-Kin Beneficiary Allocation Tables
-- Stores identity document verifications and inheritance payout beneficiaries.
-- =========================================================================

-- 1. KYC Verification & Identification Registry
CREATE TABLE member_identifications (
    identification_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL,
    id_type              profile_id_type NOT NULL,
    id_number            VARCHAR(100) NOT NULL,
    issue_date           DATE,
    expiry_date          DATE,
    issuing_authority    VARCHAR(150),
    kyc_status           profile_kyc_status NOT NULL DEFAULT 'UNVERIFIED',
    verified_by_user_id  UUID,
    verification_notes   TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_mi_user FOREIGN KEY (user_id)
        REFERENCES member_profiles(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_mi_user ON member_identifications(user_id);
CREATE INDEX idx_mi_status ON member_identifications(kyc_status);

-- 2. Next-of-Kin Beneficiary Registry
CREATE TABLE next_of_kin (
    kin_id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL,
    full_name             VARCHAR(255) NOT NULL,
    relationship          VARCHAR(50) NOT NULL,
    primary_phone         VARCHAR(15) NOT NULL,
    id_number             VARCHAR(100),
    physical_address      VARCHAR(255),
    allocation_percentage DECIMAL(5,2) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_nok_user FOREIGN KEY (user_id)
        REFERENCES member_profiles(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_nok_user ON next_of_kin(user_id);
