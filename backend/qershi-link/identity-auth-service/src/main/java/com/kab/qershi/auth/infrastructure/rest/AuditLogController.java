package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.infrastructure.persistence.SpringDataAuditLogRepository;
import com.kab.qershi.auth.infrastructure.rest.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing global platform security and administrative audit log query endpoints.
 * Strictly gated to global SUPER_ADMIN actors. Preserves tenant data privacy by excluding SACCO
 * core banking operational details.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/platform/audit-logs")
@Tag(name = "Platform Security Audit Engine", description = "Endpoints for global platform SUPER_ADMIN to inspect system security, login events, and administrative logs")
public class AuditLogController {

    private final SpringDataAuditLogRepository auditLogRepository;

    public AuditLogController(SpringDataAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Fetch global platform security audit logs", description = "Retrieves a paginated list of system security events, authentication attempts, and platform operations.")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<AuditLogResponse> logs = auditLogRepository
                .findAllByOrderByTimestampDesc(PageRequest.of(page, Math.min(size, 200)))
                .map(AuditLogResponse::fromEntity);

        return ResponseEntity.ok(logs.getContent());
    }

    @GetMapping("/sacco/{saccoId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Fetch security audit logs by SACCO ID", description = "Retrieves platform administrative logs related to a specific SACCO onboarding or provisioning context.")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsBySacco(@PathVariable UUID saccoId) {
        List<AuditLogResponse> logs = auditLogRepository.findBySaccoIdOrderByTimestampDesc(saccoId).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Fetch security audit logs by User ID", description = "Retrieves authentication and security event history for a specific user ID.")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(@PathVariable UUID userId) {
        List<AuditLogResponse> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(logs);
    }
}
