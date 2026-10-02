-- 05_notification.sql: Notification Templates & Logs
CREATE TABLE IF NOT EXISTS {schema}.notification_templates (
    template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_code VARCHAR(50) NOT NULL UNIQUE,
    channel VARCHAR(20) NOT NULL DEFAULT 'SMS',
    language VARCHAR(10) NOT NULL DEFAULT 'EN',
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.notification_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_phone VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL DEFAULT 'SMS',
    template_code VARCHAR(50) NOT NULL,
    rendered_message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    vendor_response TEXT,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {schema}.sms_gateway_configs (
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

INSERT INTO {schema}.notification_templates (template_code, channel, language, content) VALUES
('OTP_CODE', 'SMS', 'EN', 'Welcome to System Platform! Your Super Admin PIN is: {otpCode}'),
('ACCOUNT_OPENED_ALERT', 'SMS', 'EN', 'Dear {memberName}, your {productName} account {accountNo} has been successfully opened.'),
('CASH_DEPOSIT_ALERT', 'SMS', 'EN', 'Dear {memberName}, {amount} ETB has been deposited to account {accountNo}. New balance: {balance} ETB.'),
('CASH_WITHDRAWAL_ALERT', 'SMS', 'EN', 'Dear {memberName}, {amount} ETB has been withdrawn from account {accountNo}. New balance: {balance} ETB.'),
('TRANSFER_SENT_ALERT', 'SMS', 'EN', 'Dear {memberName}, you transferred {amount} ETB to {receiverName} ({receiverAccountNo}). New balance: {balance} ETB.'),
('LOAN_APPLICATION_APPROVED', 'SMS', 'EN', 'Dear {memberName}, your loan application of {amount} ETB has been APPROVED.'),
('LOAN_DISBURSED', 'SMS', 'EN', 'Dear {memberName}, your loan of {amount} ETB has been DISBURSED to your account.'),
('LOAN_REPAYMENT_CONFIRMATION', 'SMS', 'EN', 'Dear {memberName}, repayment of {amount} ETB received for loan {loanId}. Remaining balance: {remainingBalance} ETB.')
ON CONFLICT (template_code) DO NOTHING;

