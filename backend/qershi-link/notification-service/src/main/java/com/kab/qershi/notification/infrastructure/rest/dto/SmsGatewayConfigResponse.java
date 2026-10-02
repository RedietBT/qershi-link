package com.kab.qershi.notification.infrastructure.rest.dto;

import com.kab.qershi.notification.domain.model.SmsGatewayConfig;
import com.kab.qershi.notification.domain.model.SmsProviderType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload returning SACCO SMS Gateway configuration details.
 * Masks credentials for security.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Details of active SACCO SMS Gateway configuration.")
public class SmsGatewayConfigResponse {

    private UUID configId;
    private SmsProviderType provider;
    private String senderId;
    private String maskedApiKey;
    private boolean hasApiSecret;
    private String apiUrl;
    private String serviceAccountId;
    private String extraHeadersJson;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public SmsGatewayConfigResponse() {}

    public static SmsGatewayConfigResponse fromDomain(SmsGatewayConfig domain) {
        if (domain == null) return null;
        SmsGatewayConfigResponse resp = new SmsGatewayConfigResponse();
        resp.configId = domain.getConfigId();
        resp.provider = domain.getProvider();
        resp.senderId = domain.getSenderId();
        resp.maskedApiKey = domain.getMaskedApiKey();
        resp.hasApiSecret = domain.getApiSecret() != null && !domain.getApiSecret().isBlank();
        resp.apiUrl = domain.getApiUrl();
        resp.serviceAccountId = domain.getServiceAccountId();
        resp.extraHeadersJson = domain.getExtraHeadersJson();
        resp.active = domain.isActive();
        resp.createdAt = domain.getCreatedAt();
        resp.updatedAt = domain.getUpdatedAt();
        return resp;
    }

    public UUID getConfigId() { return configId; }
    public SmsProviderType getProvider() { return provider; }
    public String getSenderId() { return senderId; }
    public String getMaskedApiKey() { return maskedApiKey; }
    public boolean isHasApiSecret() { return hasApiSecret; }
    public String getApiUrl() { return apiUrl; }
    public String getServiceAccountId() { return serviceAccountId; }
    public String getExtraHeadersJson() { return extraHeadersJson; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
