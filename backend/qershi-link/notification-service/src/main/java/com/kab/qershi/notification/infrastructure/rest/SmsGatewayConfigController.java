package com.kab.qershi.notification.infrastructure.rest;

import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.SmsGatewayConfig;
import com.kab.qershi.notification.domain.ports.inbound.SmsGatewayConfigUseCase;
import com.kab.qershi.notification.infrastructure.rest.dto.NotificationResponse;
import com.kab.qershi.notification.infrastructure.rest.dto.SmsGatewayConfigRequest;
import com.kab.qershi.notification.infrastructure.rest.dto.SmsGatewayConfigResponse;
import com.kab.qershi.notification.infrastructure.rest.dto.TestSmsGatewayRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for SACCO SMS Gateway configuration and live gateway testing.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notifications/config/sms-gateway")
@Tag(name = "3. SMS Gateway Multi-Provider Engine", description = "Endpoints for configuring dynamic SACCO SMS providers and testing live gateway credentials.")
public class SmsGatewayConfigController {

    private final SmsGatewayConfigUseCase configUseCase;

    public SmsGatewayConfigController(SmsGatewayConfigUseCase configUseCase) {
        this.configUseCase = configUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('NOTIFICATION_CONFIG_MANAGE')")
    @Operation(summary = "Get Active SMS Gateway Config", description = "Retrieves active SMS provider configuration for the current SACCO tenant schema.")
    public ResponseEntity<SmsGatewayConfigResponse> getActiveConfig() {
        SmsGatewayConfig config = configUseCase.getActiveConfig();
        return ResponseEntity.ok(SmsGatewayConfigResponse.fromDomain(config));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('NOTIFICATION_CONFIG_MANAGE')")
    @Operation(summary = "Save SMS Gateway Config", description = "Creates or updates SMS provider credentials (AfroMessage, Ethio Telecom, Infobip, Webhook) for the SACCO.")
    public ResponseEntity<SmsGatewayConfigResponse> saveConfig(@Valid @RequestBody SmsGatewayConfigRequest dto) {
        SmsGatewayConfig domain = new SmsGatewayConfig(
                null,
                dto.getProvider(),
                dto.getSenderId(),
                dto.getApiKey(),
                dto.getApiSecret(),
                dto.getApiUrl(),
                dto.getServiceAccountId(),
                dto.getExtraHeadersJson(),
                dto.isActive(),
                null,
                null
        );

        SmsGatewayConfig saved = configUseCase.saveConfig(domain);
        return ResponseEntity.ok(SmsGatewayConfigResponse.fromDomain(saved));
    }

    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('NOTIFICATION_CONFIG_MANAGE', 'NOTIFICATION_SEND')")
    @Operation(summary = "Test SMS Gateway Connection", description = "Sends a live test SMS to verify provider connectivity and credentials.")
    public ResponseEntity<NotificationResponse> testConnection(@Valid @RequestBody TestSmsGatewayRequest dto) {
        NotificationLog testLog = configUseCase.testSmsGateway(dto.getRecipientPhone(), dto.getCustomMessage());
        return ResponseEntity.ok(NotificationResponse.fromDomain(testLog));
    }
}
