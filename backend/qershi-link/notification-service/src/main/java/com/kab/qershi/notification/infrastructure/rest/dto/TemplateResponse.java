package com.kab.qershi.notification.infrastructure.rest.dto;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLanguage;
import com.kab.qershi.notification.domain.model.NotificationTemplate;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for notification template queries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TemplateResponse {

    private UUID templateId;
    private String templateCode;
    private NotificationChannel channel;
    private NotificationLanguage language;
    private String content;
    private boolean active;
    private Instant createdAt;

    public TemplateResponse() {}

    public static TemplateResponse fromDomain(NotificationTemplate domain) {
        if (domain == null) return null;
        TemplateResponse dto = new TemplateResponse();
        dto.setTemplateId(domain.getTemplateId());
        dto.setTemplateCode(domain.getTemplateCode());
        dto.setChannel(domain.getChannel());
        dto.setLanguage(domain.getLanguage());
        dto.setContent(domain.getContent());
        dto.setActive(domain.isActive());
        dto.setCreatedAt(domain.getCreatedAt());
        return dto;
    }

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public NotificationLanguage getLanguage() { return language; }
    public void setLanguage(NotificationLanguage language) { this.language = language; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
