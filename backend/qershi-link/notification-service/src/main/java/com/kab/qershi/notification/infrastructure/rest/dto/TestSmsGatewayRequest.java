package com.kab.qershi.notification.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for sending a test SMS from the UI.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload to test the active SACCO SMS Gateway configuration.")
public class TestSmsGatewayRequest {

    @NotBlank(message = "Recipient phone number is required")
    @Schema(description = "Recipient phone number (e.g. +251911223344)", example = "+251911223344")
    private String recipientPhone;

    @Schema(description = "Optional test message content", example = "Testing SACCO SMS Gateway connection.")
    private String customMessage;

    public TestSmsGatewayRequest() {}

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getCustomMessage() { return customMessage; }
    public void setCustomMessage(String customMessage) { this.customMessage = customMessage; }
}
