package com.kab.qershi.account.infrastructure.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Hibernate context interceptor resolving active schema mapping tags for statement compilation in account-service.
 * Bridges ThreadLocal tracking states into the active database session coordinator.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getTenantSchema();
        return tenant != null ? tenant : TenantContext.DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
