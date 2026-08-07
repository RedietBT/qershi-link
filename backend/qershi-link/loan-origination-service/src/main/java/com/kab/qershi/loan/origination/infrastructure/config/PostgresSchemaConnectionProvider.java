package com.kab.qershi.loan.origination.infrastructure.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * PostgreSQL Schema Multi-Tenant Connection Provider.
 * Dynamically switches search_path per HTTP/gRPC request context.
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

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + tenantIdentifier + ", public");
            log.trace("PostgreSQL search_path set to tenant: {}", tenantIdentifier);
        } catch (SQLException e) {
            log.error("Error setting PostgreSQL search_path for tenant: {}", tenantIdentifier, e);
            connection.close();
            throw e;
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO public");
        } catch (SQLException e) {
            log.warn("Error resetting PostgreSQL search_path to public for tenant: {}", tenantIdentifier, e);
        } finally {
            connection.close();
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
