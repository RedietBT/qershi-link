package com.kab.qershi.profile.infrastructure.rest;

import com.kab.qershi.profile.infrastructure.persistence.SpringDataProfileAuditLogRepository;
import com.kab.qershi.profile.infrastructure.rest.dto.ProfileAuditLogResponse;
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
 * REST Controller exposing SACCO member profile audit log query endpoints.
 * Gated strictly to authorized SACCO_ADMIN and AUDITOR actors.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/profiles/audit")
@Tag(name = "Profile Audit Engine", description = "Endpoints for SACCO administrators and compliance auditors to inspect member profile creations, address, and employment modifications")
public class ProfileAuditLogController {

    private final SpringDataProfileAuditLogRepository auditLogRepository;

    public ProfileAuditLogController(SpringDataProfileAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch SACCO profile audit logs", description = "Retrieves a paginated timeline of member profile registrations and administrative modifications.")
    public ResponseEntity<List<ProfileAuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<ProfileAuditLogResponse> logs = auditLogRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, Math.min(size, 200)))
                .map(ProfileAuditLogResponse::fromEntity);

        return ResponseEntity.ok(logs.getContent());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch profile audit trail for a specific member user ID", description = "Retrieves profile modification audit logs associated with a specific member user ID.")
    public ResponseEntity<List<ProfileAuditLogResponse>> getAuditLogsByUserId(@PathVariable UUID userId) {
        List<ProfileAuditLogResponse> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ProfileAuditLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(logs);
    }
}
