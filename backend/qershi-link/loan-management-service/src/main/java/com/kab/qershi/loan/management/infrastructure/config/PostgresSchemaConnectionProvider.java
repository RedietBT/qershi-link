package com.kab.qershi.loan.management.infrastructure.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Multi-tenant connection provider setting PostgreSQL search_path dynamically per HTTP request context.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class PostgresSchemaConnectionProvider implements MultiTenantConnectionProvider<String> {

    private static final Logger log = LoggerFactory.getLogger(PostgresSchemaConnectionProvider.class);
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
        if (clean.equals(TenantContext.DEFAULT_TENANT) || clean.equals("public")) {
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
        Connection connection = getAnyConnection();
        try (Statement statement = connection.createStatement()) {
            String schema = (tenantIdentifier != null && !tenantIdentifier.isBlank())
                    ? tenantIdentifier.trim().toLowerCase()
                    : TenantContext.DEFAULT_TENANT;
            statement.execute("SET search_path TO " + schema + ", public");
            log.trace("PostgreSQL search_path switched to: {}", schema);
        } catch (SQLException ex) {
            log.error("Error setting PostgreSQL search_path for tenant {}: {}", tenantIdentifier, ex.getMessage());
            connection.close();
            throw ex;
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO public");
        } catch (SQLException ex) {
            log.warn("Failed resetting search_path to public: {}", ex.getMessage());
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
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
