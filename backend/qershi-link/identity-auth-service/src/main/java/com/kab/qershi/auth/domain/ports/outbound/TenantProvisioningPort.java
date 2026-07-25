package com.kab.qershi.auth.domain.ports.outbound;

public interface TenantProvisioningPort {

    /**
     * Executes physical CREATE SCHEMA, sets up the search_path, and runs tenant migration scripts.
     * After this completes, the schema contains all tables, permissions, and the default ADMIN role.
     */
    void provisionTenantSchema(String schemaName);

    /**
     * Drops a schema completely if an unrecoverable failure occurs during the onboarding pipeline.
     */
    void dropTenantSchema(String schemaName);
}