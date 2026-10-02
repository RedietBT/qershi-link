package com.kab.qershi.notification.domain.ports.outbound;

import com.kab.qershi.notification.domain.model.SmsGatewayConfig;

/**
 * Outbound port for interacting with external SMS / Email Gateway providers.
 * Supports static fallback as well as dynamic tenant-specific gateway configuration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface NotificationProviderPort {

    /**
     * Dispatches SMS message using default/static provider configuration.
     */
    default String sendSms(String recipientPhone, String message) {
        return sendSms(recipientPhone, message, null);
    }

    /**
     * Dispatches SMS message using dynamic tenant-specific SMS gateway configuration.
     *
     * @param recipientPhone Destination mobile number (E.164 format)
     * @param message Text message content
     * @param config Active SACCO SMS gateway configuration (credentials, sender ID, endpoint)
     * @return Vendor gateway response string or simulated response
     */
    String sendSms(String recipientPhone, String message, SmsGatewayConfig config);
}
