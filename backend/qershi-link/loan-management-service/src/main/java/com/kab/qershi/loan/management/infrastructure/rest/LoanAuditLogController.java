package com.kab.qershi.loan.management.infrastructure.rest;

import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAuditLogRepository;
import com.kab.qershi.loan.management.infrastructure.rest.dto.LoanAuditLogResponse;
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
 * REST Controller exposing SACCO loan account audit log query endpoints.
 * Gated strictly to authorized SACCO_ADMIN and AUDITOR actors.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loan-mgmt/audit")
@Tag(name = "Loan Audit Engine", description = "Endpoints for SACCO administrators and compliance auditors to inspect loan disbursements, repayments, and lifecycle state changes")
public class LoanAuditLogController {

    private final SpringDataLoanAuditLogRepository auditLogRepository;

    public LoanAuditLogController(SpringDataLoanAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch SACCO loan audit logs", description = "Retrieves a paginated timeline of loan disbursements, Maker-Checker approvals, and repayment postings.")
    public ResponseEntity<List<LoanAuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<LoanAuditLogResponse> logs = auditLogRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, Math.min(size, 200)))
                .map(LoanAuditLogResponse::fromEntity);

        return ResponseEntity.ok(logs.getContent());
    }

    @GetMapping("/account/{accountNo}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch audit trail for a specific loan account number", description = "Retrieves full history of modifications made to a specific member loan account.")
    public ResponseEntity<List<LoanAuditLogResponse>> getAuditLogsByAccountNo(@PathVariable String accountNo) {
        List<LoanAuditLogResponse> logs = auditLogRepository.findByAccountNoOrderByCreatedAtDesc(accountNo).stream()
                .map(LoanAuditLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch audit trail for a specific member user ID", description = "Retrieves loan audit logs associated with a specific member user ID.")
    public ResponseEntity<List<LoanAuditLogResponse>> getAuditLogsByUserId(@PathVariable UUID userId) {
        List<LoanAuditLogResponse> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(LoanAuditLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(logs);
    }
}
