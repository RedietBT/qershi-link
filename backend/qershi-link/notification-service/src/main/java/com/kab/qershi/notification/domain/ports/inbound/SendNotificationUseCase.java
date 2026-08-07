package com.kab.qershi.notification.domain.ports.inbound;

import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.NotificationRequest;

/**
 * Inbound port for dispatching SMS, Email, or Push notifications.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface SendNotificationUseCase {

    /**
     * Dispatches a direct text SMS notification.
     */
    NotificationLog sendDirectSms(String recipientPhone, String message);

    /**
     * Dispatches a templated notification using template code and dynamic placeholder parameters.
     */
    NotificationLog sendTemplatedNotification(NotificationRequest request);
}
