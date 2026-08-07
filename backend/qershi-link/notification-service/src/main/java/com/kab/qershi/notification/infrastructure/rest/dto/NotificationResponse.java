package com.kab.qershi.notification.infrastructure.rest.dto;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for notification delivery audit logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class NotificationResponse {

    private UUID logId;
    private String recipientPhone;
    private NotificationChannel channel;
    private String templateCode;
    private String renderedMessage;
    private NotificationStatus status;
    private String vendorResponse;
    private Instant sentAt;

    public NotificationResponse() {}

    public static NotificationResponse fromDomain(NotificationLog domain) {
        if (domain == null) return null;
        NotificationResponse dto = new NotificationResponse();
        dto.setLogId(domain.getLogId());
        dto.setRecipientPhone(domain.getRecipientPhone());
        dto.setChannel(domain.getChannel());
        dto.setTemplateCode(domain.getTemplateCode());
        dto.setRenderedMessage(domain.getRenderedMessage());
        dto.setStatus(domain.getStatus());
        dto.setVendorResponse(domain.getVendorResponse());
        dto.setSentAt(domain.getSentAt());
        return dto;
    }

    public UUID getLogId() { return logId; }
    public void setLogId(UUID logId) { this.logId = logId; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getRenderedMessage() { return renderedMessage; }
    public void setRenderedMessage(String renderedMessage) { this.renderedMessage = renderedMessage; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public String getVendorResponse() { return vendorResponse; }
    public void setVendorResponse(String vendorResponse) { this.vendorResponse = vendorResponse; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
