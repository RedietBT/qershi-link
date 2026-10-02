package com.kab.qershi.notification.domain.ports.inbound;

import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.SmsGatewayConfig;

/**
 * Inbound port for managing SACCO SMS Gateway configurations and testing connectivity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface SmsGatewayConfigUseCase {

    /**
     * Retrieves active SMS gateway configuration for the current tenant.
     * Falls back to master default if not configured.
     */
    SmsGatewayConfig getActiveConfig();

    /**
     * Saves or updates SMS gateway configuration for the current tenant.
     */
    SmsGatewayConfig saveConfig(SmsGatewayConfig config);

    /**
     * Executes real-time live connectivity test by dispatching test SMS.
     */
    NotificationLog testSmsGateway(String testPhone, String customMessage);
}
