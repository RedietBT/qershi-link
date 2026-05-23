package com.kab.qershi.auth.domain.ports.outbound;

import com.kab.qershi.auth.domain.model.Role;
import java.util.UUID;

public interface TenantProvisioningPort {

    // Executes physical CREATE SCHEMA and runs the tenant migration scripts
    void provisionTenantSchema(String schemaName);

    // Drops a schema if something fails during onboarding (Rollback mechanism)
    void dropTenantSchema(String schemaName);

    // Seeds the default ADMIN role and permissions directly into the private schema vault
    void seedTenantRbac(String schemaName, Role adminRole);
}