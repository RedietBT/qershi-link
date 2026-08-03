package com.kab.qershi.profile.infrastructure.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Spring Boot & Hibernate 6 Multi-Tenancy Configuration.
 * Binds PostgresSchemaConnectionProvider and TenantIdentifierResolver to Hibernate ORM,
 * enabling dynamic per-request 'SET search_path TO <tenant_schema>, master_schema, public' schema switching.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Configuration
public class HibernateMultiTenancyConfig implements HibernatePropertiesCustomizer {

    private final PostgresSchemaConnectionProvider connectionProvider;
    private final TenantIdentifierResolver tenantResolver;

    public HibernateMultiTenancyConfig(PostgresSchemaConnectionProvider connectionProvider,
                                       TenantIdentifierResolver tenantResolver) {
        this.connectionProvider = connectionProvider;
        this.tenantResolver = tenantResolver;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
    }
}
