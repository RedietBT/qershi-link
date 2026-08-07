package com.kab.qershi.notification.domain.model;

import java.util.Map;

/**
 * Value Object representing an incoming notification dispatch request.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class NotificationRequest {

    private String recipientPhone;
    private String templateCode;
    private String rawMessage;
    private Map<String, String> parameters;
    private NotificationChannel channel;
    private NotificationLanguage language;

    public NotificationRequest(String recipientPhone, String templateCode, String rawMessage,
                               Map<String, String> parameters, NotificationChannel channel,
                               NotificationLanguage language) {
        this.recipientPhone = recipientPhone;
        this.templateCode = templateCode;
        this.rawMessage = rawMessage;
        this.parameters = parameters;
        this.channel = channel != null ? channel : NotificationChannel.SMS;
        this.language = language != null ? language : NotificationLanguage.EN;
    }

    public String getRecipientPhone() { return recipientPhone; }
    public String getTemplateCode() { return templateCode; }
    public String getRawMessage() { return rawMessage; }
    public Map<String, String> getParameters() { return parameters; }
    public NotificationChannel getChannel() { return channel; }
    public NotificationLanguage getLanguage() { return language; }
}
