package com.kab.qershi.notification.infrastructure.rest.dto;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLanguage;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * DTO for requesting SMS notification dispatches.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class SendSmsRequest {

    @NotBlank(message = "Recipient phone number is required.")
    private String recipientPhone;

    private String templateCode;
    private String rawMessage;
    private Map<String, String> parameters;
    private NotificationChannel channel = NotificationChannel.SMS;
    private NotificationLanguage language = NotificationLanguage.EN;
    private String providerBeanName; // Optional SMS provider override

    public SendSmsRequest() {}

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }

    public Map<String, String> getParameters() { return parameters; }
    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public NotificationLanguage getLanguage() { return language; }
    public void setLanguage(NotificationLanguage language) { this.language = language; }

    public String getProviderBeanName() { return providerBeanName; }
    public void setProviderBeanName(String providerBeanName) { this.providerBeanName = providerBeanName; }
}
