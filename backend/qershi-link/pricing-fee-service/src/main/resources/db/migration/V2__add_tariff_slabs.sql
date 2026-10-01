-- =========================================================================
-- V2: Tiered / Amount-Bracket Slab Pricing Engine
-- Allows tariffs to define sequential amount tiers (e.g. 0-1k: 5 ETB, 1k-10k: 15 ETB, 10k+: 0.25%)
-- =========================================================================

CREATE TABLE IF NOT EXISTS tariff_slabs (
    slab_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tariff_id           UUID NOT NULL,
    slab_order          INT NOT NULL DEFAULT 1,
    from_amount         DECIMAL(15,4) NOT NULL DEFAULT 0.0000,
    to_amount           DECIMAL(15,4), -- NULL or 0 represents unbounded / infinite upper ceiling
    fee_type            VARCHAR(20) NOT NULL DEFAULT 'FLAT', -- 'FLAT', 'PERCENTAGE'
    fee_value           DECIMAL(15,4) NOT NULL DEFAULT 0.0000,
    min_fee             DECIMAL(15,2),
    max_fee             DECIMAL(15,2),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tariff_slabs_tariff FOREIGN KEY (tariff_id) REFERENCES tariffs(tariff_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tariff_slabs_tariff_id ON tariff_slabs(tariff_id);
CREATE INDEX IF NOT EXISTS idx_tariff_slabs_lookup ON tariff_slabs(tariff_id, from_amount, to_amount);

-- Seed an Enterprise Tiered Cash Withdrawal Tariff schedule
INSERT INTO tariffs (tariff_code, tariff_name, transaction_type, fee_type, fee_value, min_fee, max_fee, fee_gl_code, description)
VALUES
('TAR-WTH-TIER', 'Tiered OTC Cash Withdrawal Tariff', 'WITHDRAWAL_TIERED', 'TIERED', 0.0000, NULL, NULL, '4020', 'Tiered bracket withdrawal schedule: 0-1k (5 ETB), 1k-10k (15 ETB), 10k-50k (25 ETB), 50k+ (0.25% max 100 ETB)')
ON CONFLICT (tariff_code) DO NOTHING;

-- Populate slabs for TAR-WTH-TIER
DO $$
DECLARE
    v_tariff_id UUID;
BEGIN
    SELECT tariff_id INTO v_tariff_id FROM tariffs WHERE tariff_code = 'TAR-WTH-TIER';
    IF v_tariff_id IS NOT NULL THEN
        INSERT INTO tariff_slabs (tariff_id, slab_order, from_amount, to_amount, fee_type, fee_value, min_fee, max_fee)
        VALUES
        (v_tariff_id, 1, 0.0000, 1000.0000, 'FLAT', 5.0000, NULL, NULL),
        (v_tariff_id, 2, 1000.0100, 10000.0000, 'FLAT', 15.0000, NULL, NULL),
        (v_tariff_id, 3, 10000.0100, 50000.0000, 'FLAT', 25.0000, NULL, NULL),
        (v_tariff_id, 4, 50000.0100, NULL, 'PERCENTAGE', 0.2500, 30.00, 100.00)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;
