package com.kab.qershi.notification.infrastructure.rest;

import com.kab.qershi.notification.domain.model.NotificationTemplate;
import com.kab.qershi.notification.domain.ports.inbound.TemplateManagementUseCase;
import com.kab.qershi.notification.infrastructure.rest.dto.CreateTemplateRequest;
import com.kab.qershi.notification.infrastructure.rest.dto.TemplateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller exposing SMS notification template management endpoints.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notifications/templates")
@Tag(name = "2. Template Management Engine", description = "Endpoints for creating, updating, and viewing SACCO notification templates.")
public class TemplateController {

    private final TemplateManagementUseCase templateManagementUseCase;

    public TemplateController(TemplateManagementUseCase templateManagementUseCase) {
        this.templateManagementUseCase = templateManagementUseCase;
    }

    @PostMapping
    @Operation(summary = "Create Notification Template", description = "Creates a new multi-language message template with dynamic placeholders.")
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody CreateTemplateRequest dto) {
        NotificationTemplate created = templateManagementUseCase.createTemplate(
                dto.getTemplateCode(),
                dto.getChannel(),
                dto.getLanguage(),
                dto.getContent()
        );
        return ResponseEntity.ok(TemplateResponse.fromDomain(created));
    }

    @GetMapping
    @Operation(summary = "List All Templates", description = "Lists all notification templates for the active tenant schema.")
    public ResponseEntity<List<TemplateResponse>> listTemplates() {
        List<NotificationTemplate> templates = templateManagementUseCase.getAllTemplates();
        List<TemplateResponse> dtos = templates.stream()
                .map(TemplateResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get Template by Code", description = "Retrieves a specific notification template by code (e.g. CASH_DEPOSIT_ALERT).")
    public ResponseEntity<TemplateResponse> getTemplateByCode(@PathVariable String code) {
        NotificationTemplate template = templateManagementUseCase.getTemplateByCode(code);
        return ResponseEntity.ok(TemplateResponse.fromDomain(template));
    }

    @PutMapping("/{code}")
    @Operation(summary = "Update Template", description = "Updates content text or active status of a notification template.")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable String code,
            @RequestParam(required = false) String content,
            @RequestParam(defaultValue = "true") boolean active) {
        NotificationTemplate updated = templateManagementUseCase.updateTemplate(code, content, active);
        return ResponseEntity.ok(TemplateResponse.fromDomain(updated));
    }
}
