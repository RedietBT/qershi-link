-- =========================================================================
-- V2: Core Banking Custom ENUM Types for Account Management
-- =========================================================================

-- 1. Member Account Lifecycle Statuses
CREATE TYPE account_status AS ENUM (
    'PENDING_APPROVAL',
    'ACTIVE',
    'DORMANT',
    'FROZEN',
    'CLOSED'
);

-- 3. Administrative Freeze Controls
CREATE TYPE freeze_status AS ENUM (
    'NONE',
    'DEBIT_FREEZE',
    'CREDIT_FREEZE',
    'FULL_FREEZE'
);

-- 4. Monetary Lien Hold Statuses
CREATE TYPE lien_status AS ENUM (
    'ACTIVE',
    'RELEASED',
    'EXPIRED'
);

-- 5. Interest Capitalization Posting Frequency
CREATE TYPE interest_posting_frequency AS ENUM (
    'MONTHLY',
    'QUARTERLY',
    'SEMI_ANNUALLY',
    'ANNUALLY',
    'AT_MATURITY'
);
