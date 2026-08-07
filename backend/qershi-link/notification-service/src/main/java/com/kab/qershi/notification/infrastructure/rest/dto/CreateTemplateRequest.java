package com.kab.qershi.notification.infrastructure.rest.dto;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLanguage;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new notification template.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class CreateTemplateRequest {

    @NotBlank(message = "Template code is required.")
    private String templateCode;

    private NotificationChannel channel = NotificationChannel.SMS;
    private NotificationLanguage language = NotificationLanguage.EN;

    @NotBlank(message = "Template content text is required.")
    private String content;

    public CreateTemplateRequest() {}

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public NotificationLanguage getLanguage() { return language; }
    public void setLanguage(NotificationLanguage language) { this.language = language; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
