package com.kab.qershi.notification.infrastructure.adapters;

import com.kab.qershi.notification.domain.model.SmsGatewayConfig;
import com.kab.qershi.notification.domain.ports.outbound.NotificationProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulated SMS Adapter for testing, staging, and local environments.
 * Logs message payload without incurring SMS carrier charges.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component("simulatedSmsAdapter")
public class SimulatedSmsAdapter implements NotificationProviderPort {

    private static final Logger log = LoggerFactory.getLogger(SimulatedSmsAdapter.class);

    @Override
    public String sendSms(String recipientPhone, String message, SmsGatewayConfig config) {
        log.info("[SIMULATED SMS] Destination: {}, Sender: {}, Message: '{}'",
                recipientPhone,
                config != null && config.getSenderId() != null ? config.getSenderId() : "QERSHI",
                message);

        return "{\"status\":\"SUCCESS\",\"provider\":\"SIMULATED\",\"detail\":\"Message processed in simulation mode\",\"recipient\":\"" + recipientPhone + "\"}";
    }
}
