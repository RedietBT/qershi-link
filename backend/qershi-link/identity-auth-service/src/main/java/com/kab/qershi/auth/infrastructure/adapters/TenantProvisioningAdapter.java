package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.ports.outbound.TenantProvisioningPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Decoupled infrastructure outbound adapter handling programmatic PostgreSQL schema provisioning.
 * Loads modular DDL SQL templates from classpath resources (src/main/resources/tenant-template/*.sql)
 * and provisions multi-tenant domain tables and security RBAC configurations dynamically.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@Component
public class TenantProvisioningAdapter implements TenantProvisioningPort {

    private static final Logger log = LoggerFactory.getLogger(TenantProvisioningAdapter.class);

    /**
     * Strict whitelist pattern for PostgreSQL schema names.
     * Enforces: lowercase start character, followed by lowercase letters, digits, or underscores only.
     * Rejects any input containing special characters, spaces, quotes, or SQL metacharacters.
     */
    private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("^[a-z][a-z0-9_]{1,62}$");

    private final JdbcTemplate jdbcTemplate;
    private final ResourcePatternResolver resourcePatternResolver;

    public TenantProvisioningAdapter(JdbcTemplate jdbcTemplate, ResourcePatternResolver resourcePatternResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * Validates a schema name against a strict whitelist regex before any SQL execution.
     * This is the primary defence against SQL injection via schema name concatenation.
     *
     * @param schemaName The schema name to validate.
     * @throws IllegalArgumentException if the name contains any disallowed characters.
     */
    private void validateSchemaName(String schemaName) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalArgumentException("Schema name must not be null or blank.");
        }
        if (!SAFE_SCHEMA_NAME.matcher(schemaName).matches()) {
            throw new IllegalArgumentException(
                    "Invalid schema name '" + schemaName + "'. " +
                    "Only lowercase letters (a-z), digits (0-9), and underscores (_) are allowed. " +
                    "Name must start with a letter and be at most 63 characters."
            );
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void provisionTenantSchema(String schemaName) {
        // 0. Security Guard: Reject any schema name that does not match the strict whitelist.
        validateSchemaName(schemaName);

        log.info("Beginning modular tenant schema provisioning for '{}'", schemaName);

        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:tenant-template/*.sql");
            Arrays.sort(resources, Comparator.comparing(Resource::getFilename));

            if (resources.length == 0) {
                throw new IllegalStateException("No tenant schema template scripts found under classpath:tenant-template/");
            }

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                log.debug("Applying tenant template '{}' to schema '{}'", filename, schemaName);

                String rawSql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String renderedSql = rawSql.replace("{schema}", schemaName);

                jdbcTemplate.execute(renderedSql);
            }

            log.info("Successfully provisioned all domain modules for tenant schema '{}'", schemaName);

        } catch (IOException ex) {
            log.error("Failed to read tenant schema template scripts for '{}': {}", schemaName, ex.getMessage(), ex);
            throw new RuntimeException("Failed to load tenant provisioning SQL templates", ex);
        } catch (Exception ex) {
            log.error("Database error while provisioning tenant schema '{}': {}", schemaName, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dropTenantSchema(String schemaName) {
        // 0. Security Guard: Validate name format before any SQL execution.
        validateSchemaName(schemaName);

        // 1. Hard block: Forbid dropping protected platform-level schemas regardless of input.
        String sanitized = schemaName.trim().toLowerCase();
        if (sanitized.equals("public") || sanitized.equals("master_schema")) {
            throw new IllegalArgumentException(
                    "Security Guard: Dropping fundamental system platform namespaces is strictly prohibited."
            );
        }

        log.warn("Dropping tenant schema '{}'", sanitized);
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + sanitized + " CASCADE");
    }
}