-- =========================================================================
-- V2: Peer Guarantors Schema for Loan Origination Service (LOS)
-- Records member peer guarantors pledging savings lien collateral for loan applications.
-- =========================================================================

CREATE TABLE IF NOT EXISTS loan_guarantors (
    guarantor_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID NOT NULL,
    guarantor_user_id   UUID NOT NULL,
    guarantor_name      VARCHAR(150),
    guarantor_phone     VARCHAR(20),
    savings_account_no  VARCHAR(50) NOT NULL,
    guaranteed_amount   DECIMAL(15,2) NOT NULL,
    lien_id             UUID,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lg_app FOREIGN KEY (application_id) REFERENCES loan_applications(application_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lg_app_id ON loan_guarantors(application_id);
CREATE INDEX IF NOT EXISTS idx_lg_guarantor_user ON loan_guarantors(guarantor_user_id);
CREATE INDEX IF NOT EXISTS idx_lg_account_no ON loan_guarantors(savings_account_no);
CREATE INDEX IF NOT EXISTS idx_lg_status ON loan_guarantors(status);
