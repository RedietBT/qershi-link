package com.kab.qershi.auth.infrastructure.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

/**
 * Connection management handler orchestrating physical PostgreSQL schema context switches.
 * Executes native search_path modifications to keep data isolated without multi-pool performance overhead.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Component
public class PostgresSchemaConnectionProvider implements MultiTenantConnectionProvider<String> {

    /**
     * Strict whitelist pattern for PostgreSQL schema names.
     * Must match exactly the same rule used in TenantProvisioningAdapter.
     */
    private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("^[a-z][a-z0-9_]{1,62}$");

    private final DataSource dataSource;

    /**
     * Injection constructor binding the primary infrastructure database source connection pool.
     *
     * @param dataSource The root connection pool data source instance.
     */
    public PostgresSchemaConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Validates a tenant identifier against a strict whitelist before it is used in any SQL.
     * Rejects any value containing SQL metacharacters, quotes, semicolons, or spaces.
     *
     * @param tenantIdentifier The schema name to validate.
     * @throws IllegalArgumentException if the identifier is unsafe.
     */
    private void validateTenantIdentifier(String tenantIdentifier) {
        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            throw new IllegalArgumentException("Tenant identifier must not be null or blank.");
        }
        // Allow the system default schema without regex restriction
        if (tenantIdentifier.equals(TenantContext.DEFAULT_TENANT)) {
            return;
        }
        if (!SAFE_SCHEMA_NAME.matcher(tenantIdentifier).matches()) {
            throw new IllegalArgumentException(
                "Unsafe tenant identifier rejected: '" + tenantIdentifier + "'. " +
                "Only lowercase letters, digits, and underscores are permitted."
            );
        }
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    /**
     * Retrieves an active connection and modifies its schema execution boundary context path.
     * Protects the pipeline by isolating data scopes before queries execute.
     *
     * @param tenantIdentifier The sanitized schema name representing the target tenant vault.
     * @return Connection An active database connection pointing to the tenant's isolated space.
     * @throws SQLException If database routing execution parameters fail.
     */
    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        // Security Guard: Validate the identifier before embedding it in SQL.
        validateTenantIdentifier(tenantIdentifier);

        final Connection connection = getAnyConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + tenantIdentifier + ", public;");
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
        return connection;
    }

    /**
     * Releases connections back into the shared resource collection after resetting their search paths.
     *
     * @param tenantIdentifier The identifier of the tenant context being abandoned.
     * @param connection       The database connection instance to reset and return to the pool.
     * @throws SQLException    If context tracking state reset routines fail.
     */
    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        // Reset the search_path to the safe default regardless of what the tenant was.
        // No need to validate here — we always reset to the known-safe DEFAULT_TENANT constant.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + TenantContext.DEFAULT_TENANT + ", public;");
        } catch (SQLException ex) {
            // Force terminate connection safety structures if context state reset errors occur
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    // 🛠️ FIXED: Renamed from isUnwrappableFrom to isUnwrappableAs to match Hibernate 6 interface specifications
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return unwrapType.isInstance(this);
    }

    // 🛠️ FIXED: Safely unwraps the provider instance or throws standard Hibernate exception context
    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (unwrapType.isInstance(this)) {
            return unwrapType.cast(this);
        }
        throw new org.hibernate.service.UnknownUnwrapTypeException(unwrapType);
    }
}