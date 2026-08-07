package com.kab.qershi.loan.management.infrastructure.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Spring Boot & Hibernate 6 Multi-Tenancy Configuration.
 * Implements HibernatePropertiesCustomizer so Spring Boot manages EntityManagerFactory,
 * Hikari pool, and dialect detection while binding schema connection providers.
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
