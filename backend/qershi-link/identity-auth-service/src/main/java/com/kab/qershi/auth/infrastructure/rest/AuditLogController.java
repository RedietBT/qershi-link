package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.infrastructure.persistence.AuditLogEntity;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataAuditLogRepository;
import com.kab.qershi.auth.infrastructure.rest.dto.AuditLogResponse;
import com.kab.qershi.auth.infrastructure.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataUserRepository;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Set;
import java.util.Map;

/**
 * REST Controller exposing security and administrative audit log query endpoints.
 * Supports SUPER_ADMIN global platform inspection as well as SACCO_ADMIN tenant-scoped audit tracking.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/v1/platform/audit-logs")
@Tag(name = "Platform Security Audit Engine", description = "Endpoints for inspecting system security, login events, and administrative logs")
public class AuditLogController {

    private final SpringDataAuditLogRepository auditLogRepository;
    private final SpringDataUserRepository userRepository;

    public AuditLogController(SpringDataAuditLogRepository auditLogRepository, SpringDataUserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    private List<AuditLogResponse> mapToResponses(List<AuditLogEntity> entities) {
        Set<UUID> userIds = entities.stream()
                .map(AuditLogEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, String> msisdnMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, UserEntity::getMsisdn));

        return entities.stream()
                .map(log -> AuditLogResponse.fromEntity(log, msisdnMap.get(log.getUserId())))
                .toList();
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Fetch global platform security audit logs", description = "Retrieves a paginated list of system security events across all platform tenants. Gated strictly to SUPER_ADMIN.")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<AuditLogEntity> logsPage = auditLogRepository
                .findAllByOrderByTimestampDesc(PageRequest.of(page, Math.min(size, 200)));

        return ResponseEntity.ok(mapToResponses(logsPage.getContent()));
    }

    @GetMapping("/tenant")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch authentication audit logs for tenant SACCO", description = "Retrieves login, PIN rotation, and security events for users within the authenticated SACCO_ADMIN's SACCO.")
    public ResponseEntity<List<AuditLogResponse>> getTenantAuditLogs(Authentication authentication) {
        UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
        if (tenantSaccoId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<AuditLogEntity> logs = auditLogRepository.findBySaccoIdOrderByTimestampDesc(tenantSaccoId);
        return ResponseEntity.ok(mapToResponses(logs));
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

        List<AuditLogEntity> logs = auditLogRepository.findBySaccoIdOrderByTimestampDesc(saccoId);
        return ResponseEntity.ok(mapToResponses(logs));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch security audit logs by User ID", description = "Retrieves authentication and security event history for a specific user ID. Enforces tenant boundary for SACCO_ADMIN actors.")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable UUID userId,
            Authentication authentication) {

        List<AuditLogEntity> userLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);

        if (!SecurityUtils.isSuperAdmin(authentication)) {
            UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
            // Filter logs to ensure SACCO_ADMIN only sees logs matching their tenant SACCO
            userLogs = userLogs.stream()
                    .filter(log -> log.getSaccoId() != null && log.getSaccoId().equals(tenantSaccoId))
                    .toList();
        }

        return ResponseEntity.ok(mapToResponses(userLogs));
    }
}
