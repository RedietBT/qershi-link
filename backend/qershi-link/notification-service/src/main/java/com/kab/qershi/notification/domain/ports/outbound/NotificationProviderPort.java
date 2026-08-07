package com.kab.qershi.notification.domain.ports.outbound;

/**
 * Outbound port for interacting with external SMS / Email Gateway providers (e.g. AfroMessage).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface NotificationProviderPort {

    /**
     * Dispatches SMS message to external SMS provider API.
     * Returns vendor response string (or error details).
     */
    String sendSms(String recipientPhone, String message);
}
