package com.kab.qershi.notification.application.usecase;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLanguage;
import com.kab.qershi.notification.domain.model.NotificationTemplate;
import com.kab.qershi.notification.domain.ports.inbound.TemplateManagementUseCase;
import com.kab.qershi.notification.domain.ports.outbound.NotificationRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service implementing TemplateManagementUseCase for SACCO tenant template management.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class TemplateManagementService implements TemplateManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(TemplateManagementService.class);
    private final NotificationRepositoryPort repositoryPort;

    public TemplateManagementService(NotificationRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public NotificationTemplate createTemplate(String templateCode, NotificationChannel channel,
                                                 NotificationLanguage language, String content) {
        if (templateCode == null || templateCode.isBlank()) {
            throw new IllegalArgumentException("Template code cannot be null or blank.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Template content cannot be null or blank.");
        }

        String formattedCode = templateCode.trim().toUpperCase();
        log.info("Creating notification template [Code: {}]", formattedCode);

        NotificationTemplate template = new NotificationTemplate(
                null,
                formattedCode,
                channel != null ? channel : NotificationChannel.SMS,
                language != null ? language : NotificationLanguage.EN,
                content,
                true,
                Instant.now()
        );

        return repositoryPort.saveTemplate(template);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplate getTemplateByCode(String templateCode) {
        if (templateCode == null || templateCode.isBlank()) {
            throw new IllegalArgumentException("Template code cannot be null or blank.");
        }
        return repositoryPort.findTemplateByCode(templateCode.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Notification template not found for code: " + templateCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getAllTemplates() {
        return repositoryPort.findAllTemplates();
    }

    @Override
    @Transactional
    public NotificationTemplate updateTemplate(String templateCode, String content, boolean active) {
        NotificationTemplate existing = getTemplateByCode(templateCode);
        if (content != null && !content.isBlank()) {
            existing.setContent(content);
        }
        existing.setActive(active);
        log.info("Updating notification template [Code: {}, Active: {}]", existing.getTemplateCode(), active);
        return repositoryPort.saveTemplate(existing);
    }
}
