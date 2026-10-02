-- V3: Multi-Provider SMS Gateway Configuration per Tenant
CREATE TABLE IF NOT EXISTS master_schema.sms_gateway_configs (
    config_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(30) NOT NULL DEFAULT 'AFROMESSAGE',
    sender_id VARCHAR(50),
    api_key VARCHAR(255),
    api_secret VARCHAR(255),
    api_url VARCHAR(255),
    service_account_id VARCHAR(100),
    extra_headers_json TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed master fallback provider
INSERT INTO master_schema.sms_gateway_configs (
    provider, sender_id, api_url, is_active
) VALUES (
    'AFROMESSAGE', 'QERSHI', 'https://api.afromessage.com/api/send', TRUE
) ON CONFLICT DO NOTHING;
