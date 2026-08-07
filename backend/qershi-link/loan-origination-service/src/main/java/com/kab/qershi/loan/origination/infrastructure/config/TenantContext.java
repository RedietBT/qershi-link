package com.kab.qershi.loan.origination.infrastructure.config;

/**
 * ThreadLocal context holding active PostgreSQL tenant schema identifier (sacco_xxx).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public final class TenantContext {

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
