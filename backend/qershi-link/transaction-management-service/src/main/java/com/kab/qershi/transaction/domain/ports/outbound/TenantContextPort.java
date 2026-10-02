package com.kab.qershi.transaction.domain.ports.outbound;

/**
 * Outbound port providing access to active tenant execution context.
 * Decouples domain logic from infrastructure thread-local context containers.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TenantContextPort {

    String getCurrentTenantSchema();
}
