package com.kab.qershi.notification.domain.ports.inbound;

import com.kab.qershi.notification.domain.model.NotificationLog;

import java.util.List;

/**
 * Inbound port for viewing notification audit logs and delivery history.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface NotificationAuditUseCase {

    List<NotificationLog> getNotificationLogs();

    List<NotificationLog> getLogsByRecipient(String recipientPhone);
}
