package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.ports.outbound.TenantProvisioningPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infrastructure outbound adapter handling programmatic PostgreSQL schema provisioning.
 * Executes native schema generation and relational RBAC bootstrapping for isolated multi-tenant vaults.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.1
 */
@Component
public class TenantProvisioningAdapter implements TenantProvisioningPort {

    private final JdbcTemplate jdbcTemplate;

    public TenantProvisioningAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void provisionTenantSchema(String schemaName) {
        // 1. Ensure the schema exists
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);

        // 2. Relational Schema Construction: Provision isolated structural domain tables with IF NOT EXISTS
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".roles (" +
                "role_id UUID PRIMARY KEY, " +
                "role_name VARCHAR(50) NOT NULL, " +
                "is_system_defined BOOLEAN NOT NULL DEFAULT FALSE, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".permissions (" +
                "permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "resource VARCHAR(50) NOT NULL, " +
                "action VARCHAR(50) NOT NULL, " +
                "description VARCHAR(255), " +
                "is_active BOOLEAN NOT NULL DEFAULT TRUE, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "CONSTRAINT uq_" + schemaName + "_res_act UNIQUE (resource, action)" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".role_permissions (" +
                "role_id UUID NOT NULL, " +
                "permission_id UUID NOT NULL, " +
                "PRIMARY KEY (role_id, permission_id), " +
                "FOREIGN KEY (role_id) REFERENCES " + schemaName + ".roles(role_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (permission_id) REFERENCES " + schemaName + ".permissions(permission_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".user_roles (" +
                "user_id UUID NOT NULL, " +
                "role_id UUID NOT NULL, " +
                "sacco_id UUID NOT NULL, " +
                "PRIMARY KEY (user_id, role_id, sacco_id), " +
                "FOREIGN KEY (role_id) REFERENCES " + schemaName + ".roles(role_id) ON DELETE CASCADE" +
                ")");

        // 3. ENUM Types Setup for Tenant Schema
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'profile_gender') THEN " +
                "CREATE TYPE profile_gender AS ENUM ('MALE', 'FEMALE', 'OTHER'); END IF; END $$;");
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'profile_marital_status') THEN " +
                "CREATE TYPE profile_marital_status AS ENUM ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED'); END IF; END $$;");
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'profile_member_status') THEN " +
                "CREATE TYPE profile_member_status AS ENUM ('PENDING_APPROVAL', 'ACTIVE', 'SUSPENDED', 'DECEASED', 'CLOSED'); END IF; END $$;");
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'account_status') THEN " +
                "CREATE TYPE account_status AS ENUM ('PENDING_APPROVAL', 'ACTIVE', 'DORMANT', 'FROZEN', 'CLOSED'); END IF; END $$;");
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'freeze_status') THEN " +
                "CREATE TYPE freeze_status AS ENUM ('NONE', 'DEBIT_FREEZE', 'CREDIT_FREEZE', 'FULL_FREEZE'); END IF; END $$;");
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lien_status') THEN " +
                "CREATE TYPE lien_status AS ENUM ('ACTIVE', 'RELEASED', 'EXPIRED'); END IF; END $$;");
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'interest_posting_frequency') THEN " +
                "CREATE TYPE interest_posting_frequency AS ENUM ('MONTHLY', 'QUARTERLY', 'SEMI_ANNUALLY', 'ANNUALLY', 'AT_MATURITY'); END IF; END $$;");

        // 4. Member Profile Domain Tables
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".member_profiles (" +
                "user_id UUID PRIMARY KEY, " +
                "member_no VARCHAR(50) NOT NULL UNIQUE, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "middle_name VARCHAR(100) NOT NULL, " +
                "last_name VARCHAR(100) NOT NULL, " +
                "gender profile_gender NOT NULL, " +
                "date_of_birth DATE NOT NULL, " +
                "marital_status profile_marital_status NOT NULL DEFAULT 'SINGLE', " +
                "status profile_member_status NOT NULL DEFAULT 'PENDING_APPROVAL', " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".member_addresses (" +
                "address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "user_id UUID NOT NULL UNIQUE, " +
                "primary_phone VARCHAR(15) NOT NULL UNIQUE, " +
                "secondary_phone VARCHAR(15), " +
                "email VARCHAR(255), " +
                "region VARCHAR(100) NOT NULL, " +
                "zone_subcity VARCHAR(100) NOT NULL, " +
                "woreda VARCHAR(100) NOT NULL, " +
                "house_number VARCHAR(50), " +
                "FOREIGN KEY (user_id) REFERENCES " + schemaName + ".member_profiles(user_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".member_employments (" +
                "employment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "user_id UUID NOT NULL UNIQUE, " +
                "occupation_sector VARCHAR(100) NOT NULL, " +
                "employer_name VARCHAR(200), " +
                "monthly_income DECIMAL(19,4) NOT NULL DEFAULT 0.0000, " +
                "tin_number VARCHAR(30), " +
                "employee_id VARCHAR(50), " +
                "external_employee_id VARCHAR(100), " +
                "FOREIGN KEY (user_id) REFERENCES " + schemaName + ".member_profiles(user_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".member_governance (" +
                "governance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "user_id UUID NOT NULL UNIQUE, " +
                "submitted_by_user_id UUID NOT NULL, " +
                "approved_by_user_id UUID, " +
                "approval_date TIMESTAMPTZ, " +
                "remarks TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES " + schemaName + ".member_profiles(user_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".member_identifications (" +
                "identification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "user_id UUID NOT NULL, " +
                "id_type VARCHAR(50) NOT NULL, " +
                "id_number VARCHAR(100) NOT NULL, " +
                "issue_date DATE NOT NULL, " +
                "expiry_date DATE NOT NULL, " +
                "issuing_authority VARCHAR(150) NOT NULL, " +
                "kyc_status VARCHAR(50) NOT NULL DEFAULT 'UNVERIFIED', " +
                "verified_by_user_id UUID, " +
                "verification_notes TEXT, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (user_id) REFERENCES " + schemaName + ".member_profiles(user_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".next_of_kin (" +
                "beneficiary_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "user_id UUID NOT NULL, " +
                "full_name VARCHAR(200) NOT NULL, " +
                "relationship VARCHAR(100) NOT NULL, " +
                "primary_phone VARCHAR(15) NOT NULL, " +
                "allocation_percentage DECIMAL(5,2) NOT NULL DEFAULT 100.00, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (user_id) REFERENCES " + schemaName + ".member_profiles(user_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".profile_documents (" +
                "document_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "user_id UUID NOT NULL, " +
                "document_type VARCHAR(50) NOT NULL, " +
                "file_name VARCHAR(255) NOT NULL, " +
                "file_path VARCHAR(500) NOT NULL, " +
                "content_type VARCHAR(100) NOT NULL, " +
                "file_size_bytes BIGINT NOT NULL, " +
                "uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (user_id) REFERENCES " + schemaName + ".member_profiles(user_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".profile_audit_logs (" +
                "log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "user_id UUID NOT NULL, " +
                "performed_by_user_id UUID NOT NULL, " +
                "action VARCHAR(100) NOT NULL, " +
                "field_name VARCHAR(100), " +
                "old_value TEXT, " +
                "new_value TEXT, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        // 5. Account Domain Tables
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".account_products (" +
                "product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "product_code VARCHAR(10) NOT NULL UNIQUE, " +
                "product_name VARCHAR(150) NOT NULL, " +
                "category VARCHAR(50) NOT NULL, " +
                "currency VARCHAR(3) NOT NULL DEFAULT 'ETB', " +
                "interest_rate_pa DECIMAL(7,4) NOT NULL DEFAULT 0.0000, " +
                "posting_frequency interest_posting_frequency NOT NULL DEFAULT 'MONTHLY', " +
                "min_operating_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000, " +
                "min_monthly_contribution DECIMAL(19,4) NOT NULL DEFAULT 0.0000, " +
                "term_period_months INT, " +
                "early_withdrawal_penalty_pct DECIMAL(5,2) NOT NULL DEFAULT 0.00, " +
                "is_active BOOLEAN NOT NULL DEFAULT TRUE, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".accounts (" +
                "account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "account_no VARCHAR(50) NOT NULL UNIQUE, " +
                "user_id UUID NOT NULL, " +
                "sacco_code VARCHAR(20) NOT NULL, " +
                "branch_code VARCHAR(20) NOT NULL, " +
                "product_code VARCHAR(10) NOT NULL, " +
                "book_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000, " +
                "lien_hold_amount DECIMAL(19,4) NOT NULL DEFAULT 0.0000, " +
                "status account_status NOT NULL DEFAULT 'PENDING_APPROVAL', " +
                "freeze_status freeze_status NOT NULL DEFAULT 'NONE', " +
                "opened_date TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "approved_by_user_id UUID, " +
                "approval_date TIMESTAMPTZ, " +
                "closed_date TIMESTAMPTZ, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (product_code) REFERENCES " + schemaName + ".account_products(product_code) ON UPDATE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".account_liens (" +
                "lien_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "account_no VARCHAR(50) NOT NULL, " +
                "lien_amount DECIMAL(19,4) NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "reference_no VARCHAR(100), " +
                "placed_by_user_id UUID NOT NULL, " +
                "released_by_user_id UUID, " +
                "status lien_status NOT NULL DEFAULT 'ACTIVE', " +
                "placed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "released_at TIMESTAMPTZ, " +
                "FOREIGN KEY (account_no) REFERENCES " + schemaName + ".accounts(account_no) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".account_audit_logs (" +
                "log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "account_no VARCHAR(50), " +
                "user_id UUID NOT NULL, " +
                "performed_by_user_id UUID NOT NULL, " +
                "action VARCHAR(100) NOT NULL, " +
                "field_name VARCHAR(100), " +
                "old_value TEXT, " +
                "new_value TEXT, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        // 6. Transaction & Journal Posting Engine Domain Tables
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_type') THEN " +
                "CREATE TYPE transaction_type AS ENUM ('CASH_DEPOSIT', 'CASH_WITHDRAWAL', 'MEMBER_TRANSFER', 'SYSTEM_FEE', 'INTEREST_PAYOUT', 'REVERSAL'); END IF; END $$;");
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_status') THEN " +
                "CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'); END IF; END $$;");
        jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'entry_type') THEN " +
                "CREATE TYPE entry_type AS ENUM ('DEBIT', 'CREDIT'); END IF; END $$;");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".transactions (" +
                "transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "transaction_ref VARCHAR(50) NOT NULL UNIQUE, " +
                "account_no VARCHAR(50) NOT NULL, " +
                "sacco_code VARCHAR(20) NOT NULL, " +
                "user_id UUID NOT NULL, " +
                "processed_by_user_id UUID NOT NULL, " +
                "transaction_type transaction_type NOT NULL, " +
                "amount DECIMAL(19,4) NOT NULL, " +
                "currency VARCHAR(3) NOT NULL DEFAULT 'ETB', " +
                "status transaction_status NOT NULL DEFAULT 'PENDING', " +
                "narration TEXT, " +
                "idempotency_key VARCHAR(100) UNIQUE, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".journal_entries (" +
                "entry_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "transaction_ref VARCHAR(50) NOT NULL, " +
                "posting_date TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "description VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (transaction_ref) REFERENCES " + schemaName + ".transactions(transaction_ref) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".journal_lines (" +
                "line_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "entry_id UUID NOT NULL, " +
                "gl_account_code VARCHAR(50) NOT NULL, " +
                "entry_type entry_type NOT NULL, " +
                "amount DECIMAL(19,4) NOT NULL, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (entry_id) REFERENCES " + schemaName + ".journal_entries(entry_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".transaction_audit_logs (" +
                "log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "transaction_ref VARCHAR(50), " +
                "account_no VARCHAR(50), " +
                "performed_by_user_id UUID NOT NULL, " +
                "action VARCHAR(100) NOT NULL, " +
                "details TEXT, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        // 6. Notification Service Tables
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".notification_templates (" +
                "template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "template_code VARCHAR(50) NOT NULL UNIQUE, " +
                "channel VARCHAR(20) NOT NULL DEFAULT 'SMS', " +
                "language VARCHAR(10) NOT NULL DEFAULT 'EN', " +
                "content TEXT NOT NULL, " +
                "is_active BOOLEAN NOT NULL DEFAULT TRUE, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".notification_logs (" +
                "log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "recipient_phone VARCHAR(20) NOT NULL, " +
                "channel VARCHAR(20) NOT NULL DEFAULT 'SMS', " +
                "template_code VARCHAR(50) NOT NULL, " +
                "rendered_message TEXT NOT NULL, " +
                "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                "vendor_response TEXT, " +
                "sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        jdbcTemplate.execute("INSERT INTO " + schemaName + ".notification_templates (template_code, channel, language, content) VALUES " +
                "('OTP_CODE', 'SMS', 'EN', 'Your Qershi Link security OTP is {otpCode}. It expires in 5 minutes.'), " +
                "('CASH_DEPOSIT_ALERT', 'SMS', 'EN', 'Dear {memberName}, {amount} ETB has been deposited into your account {accountNo}. Available balance: {balance} ETB.'), " +
                "('CASH_WITHDRAWAL_ALERT', 'SMS', 'EN', 'Dear {memberName}, {amount} ETB has been withdrawn from your account {accountNo}. Remaining balance: {balance} ETB.'), " +
                "('TRANSFER_SENT_ALERT', 'SMS', 'EN', 'Dear {memberName}, you transferred {amount} ETB to {receiverName} ({receiverAccountNo}). Remaining balance: {balance} ETB.'), " +
                "('LOAN_APPLICATION_APPROVED', 'SMS', 'EN', 'Dear {memberName}, your loan application {appNo} for {amount} ETB has been APPROVED by the Credit Committee.'), " +
                "('LOAN_REPAYMENT_DUE', 'SMS', 'EN', 'Reminder: Dear {memberName}, your loan repayment of {amount} ETB is due on {dueDate}. Please ensure sufficient account balance.') " +
                "ON CONFLICT (template_code) DO NOTHING");

        // 7. Loan Origination Service Tables (LOS)
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".loan_groups (" +
                "group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "group_name VARCHAR(100) NOT NULL, " +
                "is_formal BOOLEAN NOT NULL DEFAULT FALSE, " +
                "license_no VARCHAR(50), " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".loan_group_members (" +
                "group_id UUID NOT NULL, " +
                "user_id UUID NOT NULL, " +
                "is_leader BOOLEAN NOT NULL DEFAULT FALSE, " +
                "joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "PRIMARY KEY (group_id, user_id), " +
                "FOREIGN KEY (group_id) REFERENCES " + schemaName + ".loan_groups(group_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".loan_applications (" +
                "application_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "application_no VARCHAR(50) NOT NULL UNIQUE, " +
                "user_id UUID NOT NULL, " +
                "group_id UUID, " +
                "product_id UUID NOT NULL, " +
                "scoring_type VARCHAR(30) NOT NULL, " +
                "amount_requested DECIMAL(15,2) NOT NULL, " +
                "amount_approved DECIMAL(15,2), " +
                "status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (group_id) REFERENCES " + schemaName + ".loan_groups(group_id) ON DELETE SET NULL" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".loan_credit_scoring (" +
                "scoring_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "application_id UUID NOT NULL UNIQUE, " +
                "savings_consistency DECIMAL(5,2), " +
                "historical_yield DECIMAL(10,2), " +
                "projected_yield DECIMAL(10,2), " +
                "land_size_hectares DECIMAL(8,2), " +
                "calculated_score DECIMAL(5,2), " +
                "passed_eligibility BOOLEAN NOT NULL DEFAULT FALSE, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (application_id) REFERENCES " + schemaName + ".loan_applications(application_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".loan_collateral (" +
                "collateral_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "application_id UUID NOT NULL, " +
                "type VARCHAR(30) NOT NULL, " +
                "estimated_value DECIMAL(15,2) NOT NULL DEFAULT 0.00, " +
                "document_url TEXT, " +
                "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (application_id) REFERENCES " + schemaName + ".loan_applications(application_id) ON DELETE CASCADE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".approval_workflow_logs (" +
                "log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "application_id UUID NOT NULL, " +
                "action_by UUID NOT NULL, " +
                "action_type VARCHAR(30) NOT NULL, " +
                "remarks TEXT, " +
                "action_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                "FOREIGN KEY (application_id) REFERENCES " + schemaName + ".loan_applications(application_id) ON DELETE CASCADE" +
                ")");

        // 8. Seed Security Data
        jdbcTemplate.execute("INSERT INTO " + schemaName + ".permissions (resource, action, description) VALUES " +
                "('MEMBER',           'CREATE',       'Authority to register and onboard new SACCO members.'), " +
                "('MEMBER',           'VIEW_BASIC',   'Authority to view basic profiles of SACCO members.'), " +
                "('LOAN_REQUEST',     'CREATE',       'Authority to initiate a new loan request application.'), " +
                "('LOAN',             'APPROVE',      'Authority to review and formally approve applied loans.'), " +
                "('LOAN_APPLICATION', 'CREATE',       'Authority to submit new individual or group loan applications.'), " +
                "('LOAN_APPLICATION', 'VIEW',         'Authority to inspect loan applications and scoring profiles.'), " +
                "('LOAN_APPLICATION', 'APPROVE',      'Authority to execute Maker-Checker final loan approval.'), " +
                "('LOAN_GROUP',       'MANAGE',       'Authority to onboard and configure SACCO borrowing groups.'), " +
                "('CASH',             'DEPOSIT',      'Authority to process over-the-counter cash deposits.'), " +
                "('SAVINGS',          'WITHDRAW',     'Authority to process savings withdrawal requests.'), " +
                "('REPORT',           'VIEW_ALL',     'Authority to run and view overall SACCO financial reports.'), " +
                "('SACCO',            'ATTACH',       'Authority to link external core modules or sub-entities.'), " +
                "('USER',             'VIEW_ALL',     'Authority to list and view all user security accounts.') " +
                "ON CONFLICT (resource, action) DO NOTHING");

        jdbcTemplate.execute("INSERT INTO " + schemaName + ".roles (role_id, role_name, is_system_defined) VALUES " +
                "('018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', 'ADMIN', TRUE) " +
                "ON CONFLICT (role_id) DO NOTHING");

        jdbcTemplate.execute("INSERT INTO " + schemaName + ".role_permissions (role_id, permission_id) " +
                "SELECT '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f', permission_id " +
                "FROM " + schemaName + ".permissions WHERE is_active = TRUE " +
                "ON CONFLICT (role_id, permission_id) DO NOTHING");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dropTenantSchema(String schemaName) {
        if (schemaName == null || schemaName.trim().equalsIgnoreCase("public") || schemaName.trim().equalsIgnoreCase("master_schema")) {
            throw new IllegalArgumentException("Security Guard: Dropping fundamental system platform namespaces is strictly prohibited.");
        }
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
    }
}