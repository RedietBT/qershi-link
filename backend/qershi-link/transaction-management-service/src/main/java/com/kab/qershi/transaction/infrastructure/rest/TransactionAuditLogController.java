package com.kab.qershi.transaction.infrastructure.rest;

import com.kab.qershi.transaction.domain.ports.inbound.TransactionAuditLogUseCase;
import com.kab.qershi.transaction.infrastructure.rest.dto.TransactionAuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing SACCO financial transaction audit log query endpoints.
 * Gated strictly to authorized SACCO_ADMIN and AUDITOR actors.
 * Injects inbound use case port following Hexagonal Architecture principles.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/transactions/audit")
@Tag(name = "Transaction Audit Engine", description = "Endpoints for SACCO administrators and compliance auditors to inspect deposits, withdrawals, and member transfers")
public class TransactionAuditLogController {

    private final TransactionAuditLogUseCase transactionAuditLogUseCase;

    public TransactionAuditLogController(TransactionAuditLogUseCase transactionAuditLogUseCase) {
        this.transactionAuditLogUseCase = transactionAuditLogUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch SACCO transaction audit logs", description = "Retrieves a paginated timeline of cash deposits, withdrawals, and member transfers.")
    public ResponseEntity<List<TransactionAuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<TransactionAuditLogResponse> logs = transactionAuditLogUseCase.getAuditLogs(page, size).stream()
                .map(TransactionAuditLogResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/ref/{ref}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch audit trail by transaction reference", description = "Retrieves audit log entry for a specific financial transaction reference.")
    public ResponseEntity<List<TransactionAuditLogResponse>> getAuditLogsByTransactionRef(@PathVariable String ref) {
        List<TransactionAuditLogResponse> logs = transactionAuditLogUseCase.getAuditLogsByTransactionRef(ref).stream()
                .map(TransactionAuditLogResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/account/{accountNo}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR') or hasAuthority('AUDIT_LOG_VIEW')")
    @Operation(summary = "Fetch transaction audit trail for account number", description = "Retrieves transaction audit logs for a specific member account number.")
    public ResponseEntity<List<TransactionAuditLogResponse>> getAuditLogsByAccountNo(@PathVariable String accountNo) {
        List<TransactionAuditLogResponse> logs = transactionAuditLogUseCase.getAuditLogsByAccountNo(accountNo).stream()
                .map(TransactionAuditLogResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(logs);
    }
}
