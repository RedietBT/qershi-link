package com.kab.qershi.account.infrastructure.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connection management handler orchestrating physical PostgreSQL schema context switches for account-service.
 * Includes account_schema in the search_path so core account entities are always resolvable under tenant execution.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class PostgresSchemaConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public PostgresSchemaConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    private static final java.util.regex.Pattern SAFE_SCHEMA_PATTERN = java.util.regex.Pattern.compile("^[a-z][a-z0-9_]{1,62}$");

    private void validateTenantIdentifier(String tenantIdentifier) {
        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            return;
        }
        String clean = tenantIdentifier.trim().toLowerCase();
        if (clean.equals(TenantContext.DEFAULT_TENANT) || clean.equals("master_schema") || clean.equals("public")) {
            return;
        }
        if (!SAFE_SCHEMA_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException(
                "Unsafe tenant identifier rejected: '" + tenantIdentifier + "'. Only lowercase letters, digits, and underscores are allowed."
            );
        }
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        validateTenantIdentifier(tenantIdentifier);
        final Connection connection = getAnyConnection();
        try (Statement stmt = connection.createStatement()) {
            if (tenantIdentifier != null && !tenantIdentifier.isBlank() && !tenantIdentifier.equalsIgnoreCase(TenantContext.DEFAULT_TENANT)) {
                stmt.execute("SET search_path TO " + tenantIdentifier.trim().toLowerCase() + ", " + TenantContext.DEFAULT_TENANT + ", public;");
            } else {
                stmt.execute("SET search_path TO " + TenantContext.DEFAULT_TENANT + ", public;");
            }
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + TenantContext.DEFAULT_TENANT + ", public;");
        } catch (SQLException ex) {
            // Suppress exception on release reset
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return unwrapType.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (unwrapType.isInstance(this)) {
            return unwrapType.cast(this);
        }
        throw new org.hibernate.service.UnknownUnwrapTypeException(unwrapType);
    }
}
