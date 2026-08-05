package com.kab.qershi.transaction.infrastructure.config;

/**
 * ThreadLocal container holding the active SACCO tenant PostgreSQL schema name per request thread.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public final class TenantContext {

    public static final String DEFAULT_TENANT = "master_schema";
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static String getTenantSchema() {
        return CURRENT_TENANT.get();
    }

    public static void setTenantSchema(String tenantSchema) {
        CURRENT_TENANT.set(tenantSchema);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
