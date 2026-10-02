package com.kab.qershi.notification.domain.ports.outbound;

import com.kab.qershi.notification.domain.model.SmsGatewayConfig;

import java.util.Optional;

/**
 * Outbound port for persistence of SMS gateway configurations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface SmsGatewayConfigRepositoryPort {

    Optional<SmsGatewayConfig> findActiveConfig();

    SmsGatewayConfig save(SmsGatewayConfig config);
}
