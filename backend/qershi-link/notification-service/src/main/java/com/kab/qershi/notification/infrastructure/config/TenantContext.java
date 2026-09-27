package com.kab.qershi.notification.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocal container for multi-tenant PostgreSQL schema resolution.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public final class TenantContext {

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    public static final String DEFAULT_TENANT = "master_schema";

    private TenantContext() {}

    private static final java.util.regex.Pattern SAFE_SCHEMA_PATTERN = java.util.regex.Pattern.compile("^[a-z][a-z0-9_]{1,62}$");

    public static void setTenantSchema(String schemaName) {
        if (schemaName == null || schemaName.isBlank()) {
            clear();
            return;
        }
        String clean = schemaName.trim().toLowerCase();
        if (clean.equals(DEFAULT_TENANT) || clean.equals("public")) {
            CURRENT_TENANT.set(clean);
            log.debug("TenantContext schema set to: {}", clean);
            return;
        }
        if (!SAFE_SCHEMA_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException("Unsafe tenant schema identifier rejected: " + schemaName);
        }
        CURRENT_TENANT.set(clean);
        log.debug("TenantContext schema set to: {}", clean);
    }

    public static String getTenantSchema() {
        String tenant = CURRENT_TENANT.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : DEFAULT_TENANT;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        log.debug("TenantContext cleared.");
    }
}
