package com.kab.qershi.transaction.infrastructure.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MultiTenantConnectionProvider setting search_path to the active tenant PostgreSQL schema.
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

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        try (Statement stmt = connection.createStatement()) {
            if (tenantIdentifier != null && !tenantIdentifier.isBlank() && !tenantIdentifier.equalsIgnoreCase(TenantContext.DEFAULT_TENANT)) {
                stmt.execute("SET search_path TO " + tenantIdentifier + ", " + TenantContext.DEFAULT_TENANT + ", public;");
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
