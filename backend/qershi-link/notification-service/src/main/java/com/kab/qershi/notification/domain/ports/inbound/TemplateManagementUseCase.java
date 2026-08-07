package com.kab.qershi.notification.domain.ports.inbound;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLanguage;
import com.kab.qershi.notification.domain.model.NotificationTemplate;

import java.util.List;

/**
 * Inbound port for managing SACCO tenant notification templates.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TemplateManagementUseCase {

    NotificationTemplate createTemplate(String templateCode, NotificationChannel channel,
                                         NotificationLanguage language, String content);

    NotificationTemplate getTemplateByCode(String templateCode);

    List<NotificationTemplate> getAllTemplates();

    NotificationTemplate updateTemplate(String templateCode, String content, boolean active);
}
