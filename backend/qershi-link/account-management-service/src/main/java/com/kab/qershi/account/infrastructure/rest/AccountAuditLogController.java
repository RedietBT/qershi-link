package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.infrastructure.persistence.SpringDataAccountAuditLogRepository;
import com.kab.qershi.account.infrastructure.rest.dto.AccountAuditLogResponse;
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
 * REST Controller exposing SACCO member account audit log query endpoints.
 * Gated strictly to authorized SACCO_ADMIN and AUDITOR actors.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/account-mgmt/audit")
@Tag(name = "Account Audit Engine", description = "Endpoints for SACCO administrators and compliance auditors to inspect member account state transitions")
public class AccountAuditLogController {

    private final SpringDataAccountAuditLogRepository auditLogRepository;

    public AccountAuditLogController(SpringDataAccountAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch SACCO account audit logs", description = "Retrieves a paginated timeline of account state changes, approvals, freezes, and status switches.")
    public ResponseEntity<List<AccountAuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<AccountAuditLogResponse> logs = auditLogRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, Math.min(size, 200)))
                .map(AccountAuditLogResponse::fromEntity);

        return ResponseEntity.ok(logs.getContent());
    }

    @GetMapping("/account/{accountNo}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch audit trail for a specific account number", description = "Retrieves full history of modifications made to a specific member savings account.")
    public ResponseEntity<List<AccountAuditLogResponse>> getAuditLogsByAccountNo(@PathVariable String accountNo) {
        List<AccountAuditLogResponse> logs = auditLogRepository.findByAccountNoOrderByCreatedAtDesc(accountNo).stream()
                .map(AccountAuditLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch audit trail for a specific member user ID", description = "Retrieves account audit logs associated with a specific member user ID.")
    public ResponseEntity<List<AccountAuditLogResponse>> getAuditLogsByUserId(@PathVariable UUID userId) {
        List<AccountAuditLogResponse> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AccountAuditLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(logs);
    }
}
