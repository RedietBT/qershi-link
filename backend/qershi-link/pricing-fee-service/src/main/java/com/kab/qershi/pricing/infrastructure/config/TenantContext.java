package com.kab.qershi.pricing.infrastructure.config;

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

    public static final String DEFAULT_TENANT = "master_schema";
    private static final java.util.regex.Pattern SAFE_SCHEMA_PATTERN = java.util.regex.Pattern.compile("^[a-z][a-z0-9_]{1,62}$");

    public static void setTenantSchema(String tenantSchema) {
        if (tenantSchema == null || tenantSchema.isBlank()) {
            CURRENT_TENANT.set(DEFAULT_TENANT);
            return;
        }
        String clean = tenantSchema.trim().toLowerCase();
        if (clean.equals(DEFAULT_TENANT) || clean.equals("public")) {
            CURRENT_TENANT.set(clean);
            return;
        }
        if (!SAFE_SCHEMA_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException("Unsafe tenant schema identifier rejected: " + tenantSchema);
        }
        CURRENT_TENANT.set(clean);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
