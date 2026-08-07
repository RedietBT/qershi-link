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

    public static void setTenantSchema(String schemaName) {
        if (schemaName != null && !schemaName.isBlank()) {
            CURRENT_TENANT.set(schemaName.trim());
            log.debug("TenantContext schema set to: {}", schemaName);
        } else {
            clear();
        }
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
