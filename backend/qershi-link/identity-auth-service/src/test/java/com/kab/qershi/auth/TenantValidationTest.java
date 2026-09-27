package com.kab.qershi.auth;

import com.kab.qershi.auth.infrastructure.config.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tenant Schema Validation Security Tests")
class TenantValidationTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @ParameterizedTest(name = "Valid tenant schema should be accepted: {0}")
    @ValueSource(strings = {
        "sacco_awash",
        "sacco_123",
        "tenant_abc_def",
        "master_schema",
        "public"
    })
    void shouldAcceptValidTenantSchemas(String validSchema) {
        assertDoesNotThrow(() -> TenantContext.setTenantSchema(validSchema));
        assertEquals(validSchema.toLowerCase(), TenantContext.getTenantSchema());
    }

    @ParameterizedTest(name = "SQL injection and malformed schema should be rejected: {0}")
    @ValueSource(strings = {
        "sacco_123; DROP TABLE users; --",
        "sacco' OR '1'='1",
        "sacco-dashed",
        "1starts_with_digit",
        "sacco name with space",
        "sacco\"quote",
        "../../etc/passwd",
        "sacco$variable"
    })
    void shouldRejectMaliciousTenantSchemas(String maliciousInput) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> TenantContext.setTenantSchema(maliciousInput),
            "Expected IllegalArgumentException for input: " + maliciousInput
        );
        assertTrue(ex.getMessage().contains("Unsafe tenant schema identifier rejected"));
    }

    @Test
    @DisplayName("Null or blank schema should safely fallback to default tenant")
    void shouldFallbackToDefaultOnNullOrBlank() {
        TenantContext.setTenantSchema(null);
        assertEquals(TenantContext.DEFAULT_TENANT, TenantContext.getTenantSchema());

        TenantContext.setTenantSchema("   ");
        assertEquals(TenantContext.DEFAULT_TENANT, TenantContext.getTenantSchema());
    }
}
