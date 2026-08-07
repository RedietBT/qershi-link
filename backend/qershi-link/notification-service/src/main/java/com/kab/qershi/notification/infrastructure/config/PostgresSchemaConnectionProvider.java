package com.kab.qershi.notification.infrastructure.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Multi-Tenant Connection Provider executing PostgreSQL SET search_path TO schema statements.
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
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        try (Statement statement = connection.createStatement()) {
            String schema = (tenantIdentifier != null && !tenantIdentifier.isBlank())
                    ? tenantIdentifier
                    : TenantContext.DEFAULT_TENANT;
            statement.execute("SET search_path TO " + schema + ", public");
            log.debug("Switched PostgreSQL search_path TO: {}", schema);
        } catch (SQLException e) {
            log.error("Error setting search_path for schema {}: {}", tenantIdentifier, e.getMessage());
            releaseConnection(tenantIdentifier, connection);
            throw e;
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + TenantContext.DEFAULT_TENANT + ", public");
        } catch (SQLException e) {
            log.warn("Error resetting search_path to default: {}", e.getMessage());
        } finally {
            releaseAnyConnection(connection);
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
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
