package com.kab.qershi.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing tenant-specific SMS Gateway configuration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class SmsGatewayConfig {

    private UUID configId;
    private SmsProviderType provider;
    private String senderId;
    private String apiKey;
    private String apiSecret;
    private String apiUrl;
    private String serviceAccountId;
    private String extraHeadersJson;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public SmsGatewayConfig() {}

    public SmsGatewayConfig(UUID configId,
                            SmsProviderType provider,
                            String senderId,
                            String apiKey,
                            String apiSecret,
                            String apiUrl,
                            String serviceAccountId,
                            String extraHeadersJson,
                            boolean active,
                            Instant createdAt,
                            Instant updatedAt) {
        this.configId = configId;
        this.provider = provider != null ? provider : SmsProviderType.AFROMESSAGE;
        this.senderId = senderId;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.apiUrl = apiUrl;
        this.serviceAccountId = serviceAccountId;
        this.extraHeadersJson = extraHeadersJson;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getConfigId() { return configId; }
    public void setConfigId(UUID configId) { this.configId = configId; }

    public SmsProviderType getProvider() { return provider; }
    public void setProvider(SmsProviderType provider) { this.provider = provider; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getServiceAccountId() { return serviceAccountId; }
    public void setServiceAccountId(String serviceAccountId) { this.serviceAccountId = serviceAccountId; }

    public String getExtraHeadersJson() { return extraHeadersJson; }
    public void setExtraHeadersJson(String extraHeadersJson) { this.extraHeadersJson = extraHeadersJson; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Returns masked API Key for safe display in UI/logs.
     */
    public String getMaskedApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            return "";
        }
        if (apiKey.length() <= 8) {
            return "••••••••";
        }
        return "••••••••" + apiKey.substring(apiKey.length() - 4);
    }
}
