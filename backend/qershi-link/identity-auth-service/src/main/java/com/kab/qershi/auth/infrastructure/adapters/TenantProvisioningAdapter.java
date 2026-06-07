package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.model.Role;
import com.kab.qershi.auth.domain.ports.outbound.TenantProvisioningPort;
import com.kab.qershi.auth.infrastructure.persistence.RoleEntity;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataRoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Infrastructure outbound adapter handling programmatic PostgreSQL schema provisioning.
 * Executes native schema generation and direct seeding tasks for multi-tenant isolation vaults.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TenantProvisioningAdapter implements TenantProvisioningPort {

    private final JdbcTemplate jdbcTemplate;
    private final SpringDataRoleRepository roleRepository;

    /**
     * Constructs the provisioner adapter using spring JDBC utilities and default persistence repositories.
     */
    public TenantProvisioningAdapter(JdbcTemplate jdbcTemplate, SpringDataRoleRepository roleRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleRepository = roleRepository;
    }

    /**
     * Creates a new physical PostgreSQL schema namespace and seeds the core required tables.
     * Uses Requires New propagation to separate database DDL creation locks from standard business records.
     *
     * @param schemaName The fully sanitized name of the target database schema namespace.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void provisionTenantSchema(String schemaName) {
        // Execute the physical schema containment creation block
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);

        // Core DDL Layout Construction: Seed the required localized tables inside the newly created namespace.
        // In full setups, this can also hook directly into Flyway or Liquibase programmatic migrations.
        jdbcTemplate.execute("CREATE TABLE " + schemaName + ".roles (" +
                "role_id UUID NOT NULL PRIMARY KEY, " +
                "role_name VARCHAR(50) NOT NULL, " +
                "is_system_defined BOOLEAN NOT NULL" +
                ")");

        jdbcTemplate.execute("CREATE TABLE " + schemaName + ".role_permissions (" +
                "role_id UUID NOT NULL, " +
                "permission_code VARCHAR(100) NOT NULL, " +
                "PRIMARY KEY (role_id, permission_code), " +
                "FOREIGN KEY (role_id) REFERENCES " + schemaName + ".roles(role_id) ON DELETE CASCADE" +
                ")");
    }

    /**
     * Drops a physical schema namespace from the database instance.
     * Acts as the primary architectural cleanup mechanism enforcing your Zero-Orphan Policy.
     *
     * @param schemaName The target database schema namespace to drop.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dropTenantSchema(String schemaName) {
        if (schemaName == null || schemaName.trim().equalsIgnoreCase("public") || schemaName.trim().equalsIgnoreCase("master_schema")) {
            throw new IllegalArgumentException("Security Guard: Dropping fundamental system platform namespaces is strictly prohibited.");
        }
        // CASCADE drops all tables, keys, and views inside the namespace instantly, reclaiming database resources
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
    }

    /**
     * Seeds the initialized administrator operational roles directly into the private schema vault space.
     * Temporarily modifies connection search paths to run targets securely.
     *
     * @param schemaName The destination schema namespace context to host the role profiles.
     * @param adminRole The core domain entity representing the default system admin capabilities.
     */
    @Override
    public void seedTenantRbac(String schemaName, Role adminRole) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new IllegalStateException("Database driver context failure: Unable to locate active DataSource pool.");
        }

        // We use raw connection handling here to temporarily force a search_path bypass
        // specifically for seeding this exact schema, outside the normal request thread thread-pool context loop.
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Redirect connection pointer to the target schema vault space
            statement.execute("SET search_path TO " + schemaName + ", public;");

            // Build the infrastructure entity model
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setRoleId(adminRole.getRoleId());
            roleEntity.setRoleName(adminRole.getRoleName());
            roleEntity.setSystemDefined(adminRole.isSystemDefined());
            roleEntity.getPermissions().addAll(adminRole.getPermissions());

            // Persist the entity directly into the targeted schema space
            roleRepository.save(roleEntity);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to seed initial administrator security configurations inside schema context: " + schemaName, ex);
        }
    }
}