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
    ('OTP_CODE', 'SMS', 'EN', 'Welcome to {saccoName}! Your PIN is: {otpCode}', TRUE),
    ('ACCOUNT_OPENED_ALERT', 'SMS', 'EN', 'Dear {memberName}, welcome to {saccoName}! Your {productName} account {accountNo} has been successfully opened.', TRUE),
    ('CASH_DEPOSIT_ALERT', 'SMS', 'EN', 'Dear {memberName}, {amount} ETB has been deposited to account {accountNo} at {saccoName}. New balance: {balance} ETB.', TRUE),
    ('CASH_WITHDRAWAL_ALERT', 'SMS', 'EN', 'Dear {memberName}, {amount} ETB has been withdrawn from account {accountNo} at {saccoName}. New balance: {balance} ETB.', TRUE),
    ('TRANSFER_SENT_ALERT', 'SMS', 'EN', 'Dear {memberName}, transferred {amount} ETB to {receiverName} ({receiverAccountNo}) at {saccoName}. New balance: {balance} ETB.', TRUE),
    ('LOAN_APPLICATION_APPROVED', 'SMS', 'EN', 'Dear {memberName}, your loan application of {amount} ETB at {saccoName} has been APPROVED.', TRUE),
    ('LOAN_DISBURSED', 'SMS', 'EN', 'Dear {memberName}, your loan of {amount} ETB at {saccoName} has been DISBURSED to your account.', TRUE),
    ('LOAN_REPAYMENT_CONFIRMATION', 'SMS', 'EN', 'Dear {memberName}, repayment of {amount} ETB received for loan {loanId} at {saccoName}. Remaining balance: {remainingBalance} ETB.', TRUE)
ON CONFLICT (template_code) DO UPDATE SET content = EXCLUDED.content;
