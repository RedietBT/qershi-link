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

        // 3. Seed data using ON CONFLICT to ensure idempotency
        jdbcTemplate.execute("INSERT INTO " + schemaName + ".permissions (resource, action, description) VALUES " +
                "('MEMBER',        'CREATE',       'Authority to register and onboard new SACCO members.'), " +
                "('MEMBER',        'VIEW_BASIC',   'Authority to view basic profiles of SACCO members.'), " +
                "('LOAN_REQUEST',  'CREATE',       'Authority to initiate a new loan request application.'), " +
                "('LOAN',          'APPROVE',      'Authority to review and formally approve applied loans.'), " +
                "('CASH',          'DEPOSIT',      'Authority to process over-the-counter cash deposits.'), " +
                "('SAVINGS',       'WITHDRAW',     'Authority to process savings withdrawal requests.'), " +
                "('REPORT',        'VIEW_ALL',     'Authority to run and view overall SACCO financial reports.'), " +
                "('SACCO',         'ATTACH',       'Authority to link external core modules or sub-entities.'), " +
                "('USER',          'VIEW_ALL',     'Authority to list and view all user security accounts.') " +
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