package com.kab.qershi.loan.management.infrastructure.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Resolves current tenant schema name for Hibernate multi-tenant sessions.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantSchema();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
