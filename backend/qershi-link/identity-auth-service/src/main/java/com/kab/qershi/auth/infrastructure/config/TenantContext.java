package com.kab.qershi.auth.infrastructure.config;

/**
 * Thread-isolated container managing the active tenant's database routing context.
 * Utilizes InheritableThreadLocal to preserve schema parameters across asynchronous tasks.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.1
 */
public final class TenantContext {

    /** Default fallback schema matching global platform shared components. */
    public static final String DEFAULT_TENANT = "master_schema";

    private static final InheritableThreadLocal<String> CURRENT_TENANT = new InheritableThreadLocal<>() {
        @Override
        protected String initialValue() {
            return DEFAULT_TENANT;
        }
    };

    private TenantContext() {
        // Prevent utility class instantiation
    }

    /**
     * Updates the active thread execution context to target a specific schema namespace.
     *
     * @param tenantSchema The sanitized physical schema identifier name string.
     */
    public static void setTenantSchema(String tenantSchema) {
        if (tenantSchema == null || tenantSchema.isBlank()) {
            CURRENT_TENANT.set(DEFAULT_TENANT);
        } else {
            CURRENT_TENANT.set(tenantSchema);
        }
    }

    /**
     * Resolves the current schema target assigned to the executing thread.
     *
     * @return String The active schema identifier routing key string.
     */
    public static String getTenantSchema() {
        return CURRENT_TENANT.get();
    }

    /**
     * Purges the thread storage map to mitigate memory leak profiles in container runtimes.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}