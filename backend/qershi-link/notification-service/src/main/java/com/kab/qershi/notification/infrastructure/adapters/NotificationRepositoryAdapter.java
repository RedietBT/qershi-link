package com.kab.qershi.notification.infrastructure.adapters;

import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.NotificationTemplate;
import com.kab.qershi.notification.domain.ports.outbound.NotificationRepositoryPort;
import com.kab.qershi.notification.infrastructure.persistence.NotificationLogEntity;
import com.kab.qershi.notification.infrastructure.persistence.NotificationTemplateEntity;
import com.kab.qershi.notification.infrastructure.persistence.SpringDataNotificationLogRepository;
import com.kab.qershi.notification.infrastructure.persistence.SpringDataNotificationTemplateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Outbound persistence adapter implementing NotificationRepositoryPort.
 * Converts NotificationTemplate and NotificationLog domain models ↔ JPA Entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final SpringDataNotificationTemplateRepository templateRepository;
    private final SpringDataNotificationLogRepository logRepository;

    public NotificationRepositoryAdapter(SpringDataNotificationTemplateRepository templateRepository,
                                         SpringDataNotificationLogRepository logRepository) {
        this.templateRepository = templateRepository;
        this.logRepository = logRepository;
    }

    @Override
    public NotificationTemplate saveTemplate(NotificationTemplate template) {
        NotificationTemplateEntity entity = toEntity(template);
        NotificationTemplateEntity saved = templateRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<NotificationTemplate> findTemplateByCode(String templateCode) {
        Optional<NotificationTemplateEntity> tenantOpt = templateRepository.findByTemplateCode(templateCode);
        if (tenantOpt.isPresent() && tenantOpt.get().isActive()) {
            return tenantOpt.map(this::toDomain);
        }
        return templateRepository.findMasterFallbackTemplate(templateCode).map(this::toDomain);
    }

    @Override
    public List<NotificationTemplate> findAllTemplates() {
        return templateRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public NotificationLog saveLog(NotificationLog log) {
        NotificationLogEntity entity = toEntity(log);
        NotificationLogEntity saved = logRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<NotificationLog> findLogsByRecipientPhone(String recipientPhone) {
        return logRepository.findByRecipientPhoneOrderBySentAtDesc(recipientPhone)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationLog> findAllLogs() {
        return logRepository.findAllByOrderBySentAtDesc()
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    private NotificationTemplateEntity toEntity(NotificationTemplate domain) {
        if (domain == null) return null;
        NotificationTemplateEntity entity = new NotificationTemplateEntity();
        if (domain.getTemplateId() != null && templateRepository.existsById(domain.getTemplateId())) {
            entity.setTemplateId(domain.getTemplateId());
        }
        entity.setTemplateCode(domain.getTemplateCode());
        entity.setChannel(domain.getChannel());
        entity.setLanguage(domain.getLanguage());
        entity.setContent(domain.getContent());
        entity.setActive(domain.isActive());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        return entity;
    }

    private NotificationTemplate toDomain(NotificationTemplateEntity entity) {
        if (entity == null) return null;
        return new NotificationTemplate(
                entity.getTemplateId(),
                entity.getTemplateCode(),
                entity.getChannel(),
                entity.getLanguage(),
                entity.getContent(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    private NotificationLogEntity toEntity(NotificationLog domain) {
        if (domain == null) return null;
        NotificationLogEntity entity = new NotificationLogEntity();
        if (domain.getLogId() != null && logRepository.existsById(domain.getLogId())) {
            entity.setLogId(domain.getLogId());
        }
        entity.setRecipientPhone(domain.getRecipientPhone());
        entity.setChannel(domain.getChannel());
        entity.setTemplateCode(domain.getTemplateCode());
        entity.setRenderedMessage(domain.getRenderedMessage());
        entity.setStatus(domain.getStatus());
        entity.setVendorResponse(domain.getVendorResponse());
        if (domain.getSentAt() != null) {
            entity.setSentAt(domain.getSentAt());
        }
        return entity;
    }

    private NotificationLog toDomain(NotificationLogEntity entity) {
        if (entity == null) return null;
        return new NotificationLog(
                entity.getLogId(),
                entity.getRecipientPhone(),
                entity.getChannel(),
                entity.getTemplateCode(),
                entity.getRenderedMessage(),
                entity.getStatus(),
                entity.getVendorResponse(),
                entity.getSentAt()
        );
    }
}
