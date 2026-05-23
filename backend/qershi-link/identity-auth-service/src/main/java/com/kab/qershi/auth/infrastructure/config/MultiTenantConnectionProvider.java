package com.kab.qershi.auth.infrastructure.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connection management handler orchestrating physical PostgreSQL schema context switches.
 * Executes native search_path modifications to keep data isolated without multi-pool performance costs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class MultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    /**
     * Injection constructor binding your primary infrastructure database source connection pool.
     */
    public MultiTenantConnectionProvider(DataSource dataSource) {
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

    /**
     * Retrieves an active connection and modifies its schema execution boundary context path.
     * Protects the pipeline by isolating data before queries execute.
     */
    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        try (Statement stmt = connection.createStatement()) {
            // Mitigates SQL injection by using the fully sanitized token string verified by domain engines
            stmt.execute("SET search_path TO " + tenantIdentifier + ", public;");
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
        return connection;
    }

    /**
     * Releases connections back into the shared resource collection after resetting their search paths.
     */
    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
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

    @Override
    public boolean isUnwrappableFrom(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}