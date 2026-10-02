package com.kab.qershi.auth.domain.ports.inbound;

/**
 * Inbound port for platform-level Super Admin account initialization.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface SuperAdminUseCase {

    void registerSuperAdmin(String msisdn);
}
