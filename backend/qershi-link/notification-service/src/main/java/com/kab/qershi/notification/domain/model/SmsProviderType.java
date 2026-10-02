package com.kab.qershi.notification.domain.model;

/**
 * Enumerates supported SMS Gateway providers.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum SmsProviderType {
    AFROMESSAGE,
    ETHIO_TELECOM,
    INFOBIP,
    CUSTOM_WEBHOOK,
    SIMULATED
}
