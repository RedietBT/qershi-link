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
                String key = entry.getKey();
                String val = entry.getValue();
                renderedContent = renderedContent.replace("{" + key + "}", val);
                renderedContent = renderedContent.replace("@" + key, val);
            }
        }

        // Support user-friendly alias placeholders (e.g., @Name_of_the_user, @SACCO_NAME, @AMOUNT, @Amount)
        if (parameters.containsKey("memberName") && parameters.get("memberName") != null) {
            String val = parameters.get("memberName");
            renderedContent = renderedContent.replace("@Name_of_the_user", val);
            renderedContent = renderedContent.replace("@memberName", val);
            renderedContent = renderedContent.replace("{Name_of_the_user}", val);
        }
        if (parameters.containsKey("saccoName") && parameters.get("saccoName") != null) {
            String val = parameters.get("saccoName");
            renderedContent = renderedContent.replace("@SACCO_NAME", val);
            renderedContent = renderedContent.replace("@saccoName", val);
            renderedContent = renderedContent.replace("{SACCO_NAME}", val);
        }
        if (parameters.containsKey("amount") && parameters.get("amount") != null) {
            String val = parameters.get("amount");
            renderedContent = renderedContent.replace("@AMOUNT", val);
            renderedContent = renderedContent.replace("@Amount", val);
            renderedContent = renderedContent.replace("@amount", val);
            renderedContent = renderedContent.replace("{AMOUNT}", val);
        }
        if (parameters.containsKey("balance") && parameters.get("balance") != null) {
            String val = parameters.get("balance");
            renderedContent = renderedContent.replace("@balance", val);
            renderedContent = renderedContent.replace("@Balance", val);
            renderedContent = renderedContent.replace("{balance}", val);
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
