package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.model.AuditLog;
import com.kab.qershi.auth.domain.ports.inbound.SystemAuditUseCase;
import com.kab.qershi.auth.infrastructure.rest.dto.AuditLogResponse;
import com.kab.qershi.auth.infrastructure.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing security and administrative audit log query endpoints.
 * Injects inbound port SystemAuditUseCase.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/platform/audit-logs")
@Tag(name = "Platform Security Audit Engine", description = "Endpoints for inspecting system security, login events, and administrative logs")
public class AuditLogController {

    private final SystemAuditUseCase systemAuditUseCase;

    public AuditLogController(SystemAuditUseCase systemAuditUseCase) {
        this.systemAuditUseCase = systemAuditUseCase;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Fetch global platform security audit logs", description = "Retrieves a paginated list of system security events across all platform tenants. Gated strictly to SUPER_ADMIN.")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<AuditLog> logs = systemAuditUseCase.getAuditLogs(page, size);
        return ResponseEntity.ok(logs.stream().map(AuditLogResponse::fromDomain).toList());
    }

    @GetMapping("/tenant")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch authentication audit logs for tenant SACCO", description = "Retrieves login, PIN rotation, and security events for users within the authenticated SACCO_ADMIN's SACCO.")
    public ResponseEntity<List<AuditLogResponse>> getTenantAuditLogs(Authentication authentication) {
        UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
        if (tenantSaccoId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<AuditLog> logs = systemAuditUseCase.getTenantAuditLogs(tenantSaccoId);
        return ResponseEntity.ok(logs.stream().map(AuditLogResponse::fromDomain).toList());
    }

    @GetMapping("/sacco/{saccoId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch security audit logs by SACCO ID", description = "Retrieves security audit logs for a specific SACCO ID. Enforces tenant boundary for SACCO_ADMIN actors.")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsBySacco(
            @PathVariable UUID saccoId,
            Authentication authentication) {

        if (!SecurityUtils.isSuperAdmin(authentication)) {
            UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
            if (tenantSaccoId == null || !tenantSaccoId.equals(saccoId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        List<AuditLog> logs = systemAuditUseCase.getAuditLogsBySacco(saccoId);
        return ResponseEntity.ok(logs.stream().map(AuditLogResponse::fromDomain).toList());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch security audit logs by User ID", description = "Retrieves authentication and security event history for a specific user ID. Enforces tenant boundary for SACCO_ADMIN actors.")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable UUID userId,
            Authentication authentication) {

        boolean isSuperAdmin = SecurityUtils.isSuperAdmin(authentication);
        UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);

        List<AuditLog> userLogs = systemAuditUseCase.getAuditLogsByUser(userId, isSuperAdmin, tenantSaccoId);
        return ResponseEntity.ok(userLogs.stream().map(AuditLogResponse::fromDomain).toList());
    }
}
