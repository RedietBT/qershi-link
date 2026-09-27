package com.kab.qershi.auth;

import com.kab.qershi.auth.infrastructure.adapters.TenantProvisioningAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TenantProvisioningAdapterTest {

    private JdbcTemplate jdbcTemplate;
    private ResourcePatternResolver resourcePatternResolver;
    private TenantProvisioningAdapter adapter;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        resourcePatternResolver = mock(ResourcePatternResolver.class);
        adapter = new TenantProvisioningAdapter(jdbcTemplate, resourcePatternResolver);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "sacco; DROP TABLE users;--",
            "123_invalid",
            "sacco'--",
            "sacco\"name",
            "sacco name",
            "sacco$name",
            "-leading_dash",
            ""
    })
    @DisplayName("Should reject invalid or malicious schema names in provisionTenantSchema")
    void shouldRejectInvalidSchemaNames(String invalidSchema) {
        assertThatThrownBy(() -> adapter.provisionTenantSchema(invalidSchema))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(jdbcTemplate);
    }

    @ParameterizedTest
    @ValueSource(strings = {"public", "master_schema", "PUBLIC", "MASTER_SCHEMA"})
    @DisplayName("Should strictly forbid dropping protected platform namespaces")
    void shouldForbidDroppingProtectedSchemas(String protectedSchema) {
        assertThatThrownBy(() -> adapter.dropTenantSchema(protectedSchema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly prohibited");

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    @DisplayName("Should load template SQL files and substitute {schema} placeholder accurately")
    void shouldProvisionValidSchema() throws IOException {
        String validSchema = "sacco_awash_123";
        Resource script1 = new NamedByteArrayResource("01_rbac.sql", "CREATE SCHEMA IF NOT EXISTS {schema};".getBytes(StandardCharsets.UTF_8));
        Resource script2 = new NamedByteArrayResource("02_profile.sql", "CREATE TABLE {schema}.profiles (id INT);".getBytes(StandardCharsets.UTF_8));

        when(resourcePatternResolver.getResources("classpath:tenant-template/*.sql"))
                .thenReturn(new Resource[]{script2, script1}); // Deliberately unsorted to verify sort order

        adapter.provisionTenantSchema(validSchema);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(2)).execute(captor.capture());

        assertThat(captor.getAllValues().get(0)).isEqualTo("CREATE SCHEMA IF NOT EXISTS sacco_awash_123;");
        assertThat(captor.getAllValues().get(1)).isEqualTo("CREATE TABLE sacco_awash_123.profiles (id INT);");
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        public NamedByteArrayResource(String filename, byte[] byteArray) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
