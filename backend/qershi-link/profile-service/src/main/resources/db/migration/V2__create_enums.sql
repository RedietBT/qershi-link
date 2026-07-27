-- =========================================================================
-- V2: PostgreSQL Custom ENUM Types
-- Defines domain enum types used across member profiles, KYC, and documents.
-- =========================================================================

-- Member lifecycle state
CREATE TYPE profile_member_status AS ENUM (
    'DRAFT',
    'PENDING_APPROVAL',
    'PENDING_SHARE',
    'ACTIVE',
    'SUSPENDED',
    'DECEASED',
    'CLOSED'
);

-- Official identity document types
CREATE TYPE profile_id_type AS ENUM (
    'NATIONAL_ID',
    'PASSPORT',
    'DRIVING_LICENSE',
    'KEBELE_ID',
    'TAX_ID'
);

-- KYC document verification state
CREATE TYPE profile_kyc_status AS ENUM (
    'UNVERIFIED',
    'VERIFIED',
    'REJECTED'
);

-- Gender options
CREATE TYPE profile_gender AS ENUM (
    'MALE',
    'FEMALE',
    'OTHER'
);

-- Marital status
CREATE TYPE profile_marital_status AS ENUM (
    'SINGLE',
    'MARRIED',
    'DIVORCED',
    'WIDOWED'
);

-- Document vault categories
CREATE TYPE profile_document_type AS ENUM (
    'PASSPORT_PHOTO',
    'ID_FRONT',
    'ID_BACK',
    'SIGNATURE_SPECIMEN',
    'MEMBERSHIP_APPLICATION'
);
