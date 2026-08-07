package com.kab.qershi.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an audit log entry for a dispatched notification.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class NotificationLog {

    private UUID logId;
    private String recipientPhone;
    private NotificationChannel channel;
    private String templateCode;
    private String renderedMessage;
    private NotificationStatus status;
    private String vendorResponse;
    private Instant sentAt;

    public NotificationLog(UUID logId, String recipientPhone, NotificationChannel channel,
                           String templateCode, String renderedMessage, NotificationStatus status,
                           String vendorResponse, Instant sentAt) {
        this.logId = logId;
        this.recipientPhone = recipientPhone;
        this.channel = channel != null ? channel : NotificationChannel.SMS;
        this.templateCode = templateCode;
        this.renderedMessage = renderedMessage;
        this.status = status != null ? status : NotificationStatus.PENDING;
        this.vendorResponse = vendorResponse;
        this.sentAt = sentAt != null ? sentAt : Instant.now();
    }

    public UUID getLogId() { return logId; }
    public String getRecipientPhone() { return recipientPhone; }
    public NotificationChannel getChannel() { return channel; }
    public String getTemplateCode() { return templateCode; }
    public String getRenderedMessage() { return renderedMessage; }
    public NotificationStatus getStatus() { return status; }
    public String getVendorResponse() { return vendorResponse; }
    public Instant getSentAt() { return sentAt; }

    public void setStatus(NotificationStatus status) { this.status = status; }
    public void setVendorResponse(String vendorResponse) { this.vendorResponse = vendorResponse; }
}
