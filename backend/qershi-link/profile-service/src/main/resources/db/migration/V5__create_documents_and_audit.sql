-- =========================================================================
-- V5: Document Vault References & Regulatory Audit Trail Tables
-- Stores storage file metadata (S3/MinIO keys) and immutable PII change logs.
-- =========================================================================

-- 1. Document Vault References (Media & Attachment Metadata)
CREATE TABLE profile_documents (
    document_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    document_type   profile_document_type NOT NULL,
    file_key        VARCHAR(512) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_pd_user FOREIGN KEY (user_id)
        REFERENCES member_profiles(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_pd_user ON profile_documents(user_id);

-- 2. Regulatory PII Audit Trail Logs
CREATE TABLE profile_audit_logs (
    log_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    modified_by_user_id UUID NOT NULL,
    action              VARCHAR(100) NOT NULL,
    field_changed       VARCHAR(100),
    old_value           TEXT,
    new_value           TEXT,
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pal_user ON profile_audit_logs(user_id);
