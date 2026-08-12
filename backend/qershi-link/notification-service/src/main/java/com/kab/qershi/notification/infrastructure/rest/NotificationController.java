package com.kab.qershi.notification.infrastructure.rest;

import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.NotificationRequest;
import com.kab.qershi.notification.domain.ports.inbound.NotificationAuditUseCase;
import com.kab.qershi.notification.domain.ports.inbound.SendNotificationUseCase;
import com.kab.qershi.notification.infrastructure.rest.dto.NotificationResponse;
import com.kab.qershi.notification.infrastructure.rest.dto.SendSmsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller exposing SMS notification dispatch and audit log endpoints.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "1. SMS Notification Engine", description = "Endpoints for sending direct & templated SMS alerts and inspecting delivery audit logs.")
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final NotificationAuditUseCase notificationAuditUseCase;

    public NotificationController(SendNotificationUseCase sendNotificationUseCase,
                                  NotificationAuditUseCase notificationAuditUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.notificationAuditUseCase = notificationAuditUseCase;
    }

    @PostMapping("/sms/send")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('NOTIFICATION_SEND')")
    @Operation(summary = "Send SMS Notification", description = "Dispatches direct text or templated SMS to a member phone number.")
    public ResponseEntity<NotificationResponse> sendSms(@Valid @RequestBody SendSmsRequest dto) {
        NotificationLog resultLog;
        if (dto.getTemplateCode() != null && !dto.getTemplateCode().isBlank()) {
            NotificationRequest domainRequest = new NotificationRequest(
                    dto.getRecipientPhone(),
                    dto.getTemplateCode(),
                    dto.getRawMessage(),
                    dto.getParameters(),
                    dto.getChannel(),
                    dto.getLanguage(),
                    dto.getProviderBeanName()
            );
            resultLog = sendNotificationUseCase.sendTemplatedNotification(domainRequest);
        } else {
            resultLog = sendNotificationUseCase.sendDirectSms(dto.getRecipientPhone(), dto.getRawMessage());
        }
        return ResponseEntity.ok(NotificationResponse.fromDomain(resultLog));
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('NOTIFICATION_LOG_VIEW')")
    @Operation(summary = "Get Notification Logs", description = "Retrieves complete audit trail of sent SMS notifications for the tenant.")
    public ResponseEntity<List<NotificationResponse>> getLogs() {
        List<NotificationLog> logs = notificationAuditUseCase.getNotificationLogs();
        List<NotificationResponse> dtos = logs.stream()
                .map(NotificationResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/logs/recipient/{phone}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('NOTIFICATION_LOG_VIEW')")
    @Operation(summary = "Get Logs by Recipient Phone", description = "Retrieves SMS delivery logs for a specific recipient phone number.")
    public ResponseEntity<List<NotificationResponse>> getLogsByPhone(@PathVariable String phone) {
        List<NotificationLog> logs = notificationAuditUseCase.getLogsByRecipient(phone);
        List<NotificationResponse> dtos = logs.stream()
                .map(NotificationResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
