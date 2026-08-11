CREATE TABLE IF NOT EXISTS master_schema.notification_templates (
    template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_code VARCHAR(50) NOT NULL UNIQUE,
    channel VARCHAR(20) NOT NULL DEFAULT 'SMS',
    language VARCHAR(10) NOT NULL DEFAULT 'EN',
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS master_schema.notification_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_phone VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL DEFAULT 'SMS',
    template_code VARCHAR(50) NOT NULL,
    rendered_message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    vendor_response TEXT,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default notification templates
INSERT INTO master_schema.notification_templates (template_code, channel, language, content, is_active)
VALUES 
    ('OTP_CODE', 'SMS', 'EN', 'Welcome to System Platform! Your Super Admin PIN is: {otpCode}', TRUE)
ON CONFLICT (template_code) DO NOTHING;
