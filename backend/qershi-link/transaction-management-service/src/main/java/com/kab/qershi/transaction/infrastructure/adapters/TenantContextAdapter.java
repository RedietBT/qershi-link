package com.kab.qershi.transaction.infrastructure.adapters;

import com.kab.qershi.transaction.domain.ports.outbound.TenantContextPort;
import com.kab.qershi.transaction.infrastructure.config.TenantContext;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Outbound Adapter implementing TenantContextPort.
 * Safely resolves the active tenant schema from ThreadLocal TenantContext.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TenantContextAdapter implements TenantContextPort {

    @Override
    public String getCurrentTenantSchema() {
        return TenantContext.getTenantSchema();
    }
}
