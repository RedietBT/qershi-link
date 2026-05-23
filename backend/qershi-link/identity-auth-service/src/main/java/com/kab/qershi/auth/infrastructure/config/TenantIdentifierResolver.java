package com.kab.qershi.auth.infrastructure.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Hibernate context interceptor resolving active schema mapping tags for statement compilation.
 * Bridges ThreadLocal tracking states into the active database session coordinator.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    /**
     * Resolves the active tenant routing path tag string.
     *
     * @return String Active database schema namespace routing identifier.
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getTenantSchema();
        return tenant != null ? tenant : TenantContext.DEFAULT_TENANT;
    }

    /**
     * Dictates whether the session tracking instance validates contextual states globally.
     *
     * @return boolean True to consistently enforce thread validation routines.
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}