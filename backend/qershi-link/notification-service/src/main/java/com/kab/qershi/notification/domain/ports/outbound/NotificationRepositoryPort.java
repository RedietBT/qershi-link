package com.kab.qershi.notification.domain.ports.outbound;

import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.NotificationTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for persistence of notification templates and logs in PostgreSQL.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface NotificationRepositoryPort {

    NotificationTemplate saveTemplate(NotificationTemplate template);

    Optional<NotificationTemplate> findTemplateByCode(String templateCode);

    List<NotificationTemplate> findAllTemplates();

    NotificationLog saveLog(NotificationLog log);

    List<NotificationLog> findLogsByRecipientPhone(String recipientPhone);

    List<NotificationLog> findAllLogs();
}
