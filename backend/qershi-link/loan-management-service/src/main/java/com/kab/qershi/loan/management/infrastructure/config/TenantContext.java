package com.kab.qershi.loan.management.infrastructure.config;

/**
 * Thread-local context holder for active tenant schema identifiers.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    public static final String DEFAULT_TENANT = "master_schema";

    private TenantContext() {}

    public static String getTenantSchema() {
        String tenant = CURRENT_TENANT.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : DEFAULT_TENANT;
    }

    public static void setTenantSchema(String tenantSchema) {
        CURRENT_TENANT.set(tenantSchema);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
