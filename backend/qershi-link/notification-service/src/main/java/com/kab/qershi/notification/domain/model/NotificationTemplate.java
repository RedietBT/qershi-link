package com.kab.qershi.notification.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain entity representing a Notification Template.
 * Contains placeholder substitution logic.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class NotificationTemplate {

    private UUID templateId;
    private String templateCode;
    private NotificationChannel channel;
    private NotificationLanguage language;
    private String content;
    private boolean active;
    private Instant createdAt;

    public NotificationTemplate(UUID templateId, String templateCode, NotificationChannel channel,
                                NotificationLanguage language, String content, boolean active, Instant createdAt) {
        this.templateId = templateId;
        this.templateCode = templateCode;
        this.channel = channel != null ? channel : NotificationChannel.SMS;
        this.language = language != null ? language : NotificationLanguage.EN;
        this.content = content;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    /**
     * Replaces placeholders like {memberName}, {amount}, {accountNo} with actual values from parameters map.
     */
    public String render(Map<String, String> parameters) {
        if (content == null || content.isBlank()) {
            return "";
        }
        if (parameters == null || parameters.isEmpty()) {
            return content;
        }

        String renderedContent = content;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                renderedContent = renderedContent.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return renderedContent;
    }

    public UUID getTemplateId() { return templateId; }
    public String getTemplateCode() { return templateCode; }
    public NotificationChannel getChannel() { return channel; }
    public NotificationLanguage getLanguage() { return language; }
    public String getContent() { return content; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }

    public void setActive(boolean active) { this.active = active; }
    public void setContent(String content) { this.content = content; }
}
