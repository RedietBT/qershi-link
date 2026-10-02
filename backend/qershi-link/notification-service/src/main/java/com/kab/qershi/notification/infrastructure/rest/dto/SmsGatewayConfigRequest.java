package com.kab.qershi.notification.infrastructure.rest.dto;

import com.kab.qershi.notification.domain.model.SmsProviderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for configuring SACCO SMS Gateway.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload to save or update SACCO SMS Gateway credentials and provider settings.")
public class SmsGatewayConfigRequest {

    @NotNull(message = "SMS Provider type is mandatory")
    @Schema(description = "SMS Provider type", example = "AFROMESSAGE")
    private SmsProviderType provider;

    @Schema(description = "Registered sender mask or alphanumeric identifier", example = "AWASH_SACCO")
    private String senderId;

    @Schema(description = "API Key, token, or password", example = "eyJhbGciOi...")
    private String apiKey;

    @Schema(description = "Optional secondary API Secret or basic auth password", example = "secret_pass")
    private String apiSecret;

    @Schema(description = "Custom endpoint URL if overriding default", example = "https://api.afromessage.com/api/send")
    private String apiUrl;

    @Schema(description = "Provider service account ID or shortcode", example = "ETH_ACC_1092")
    private String serviceAccountId;

    @Schema(description = "Optional JSON string containing custom HTTP headers")
    private String extraHeadersJson;

    @Schema(description = "Whether this gateway is active for the tenant", example = "true")
    private boolean active = true;

    public SmsGatewayConfigRequest() {}

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
}
