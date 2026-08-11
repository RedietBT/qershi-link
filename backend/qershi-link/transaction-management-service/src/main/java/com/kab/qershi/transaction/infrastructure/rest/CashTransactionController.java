package com.kab.qershi.transaction.infrastructure.rest;

import com.kab.qershi.common.dto.ApiResponse;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.ports.inbound.CashTransactionUseCase;
import com.kab.qershi.transaction.infrastructure.rest.dto.DepositRequest;
import com.kab.qershi.transaction.infrastructure.rest.dto.TransactionResponse;
import com.kab.qershi.transaction.infrastructure.rest.dto.WithdrawalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller exposing over-the-counter Cash Deposit and Withdrawal endpoints.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "1. Teller Cash Operations", description = "Endpoints for processing over-the-counter cash deposits and cash withdrawals.")
public class CashTransactionController {

    private static final Logger log = LoggerFactory.getLogger(CashTransactionController.class);
    private final CashTransactionUseCase cashTransactionUseCase;

    public CashTransactionController(CashTransactionUseCase cashTransactionUseCase) {
        this.cashTransactionUseCase = cashTransactionUseCase;
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('TRANSACTION_DEPOSIT') or hasAuthority('CASH_DEPOSIT')")
    @Operation(summary = "Process Cash Deposit", description = "Executes an over-the-counter cash deposit into a member savings account and posts balanced GL entries.")
    public ResponseEntity<ApiResponse<TransactionResponse>> processDeposit(
            @Valid @RequestBody DepositRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        UUID processedByUserId = extractCurrentUserId();
        log.info("REST Deposit Request: accountNo={}, amount={}, processedBy={}",
                request.getAccountNo(), request.getAmount(), processedByUserId);

        Transaction tx = cashTransactionUseCase.processDeposit(
                request.getAccountNo(),
                request.getAmount(),
                request.getNarration(),
                idempotencyKey,
                processedByUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(TransactionResponse.fromDomain(tx), "Cash deposit processed successfully."));
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('TRANSACTION_WITHDRAW') or hasAuthority('SAVINGS_WITHDRAW')")
    @Operation(summary = "Process Cash Withdrawal", description = "Executes an over-the-counter cash withdrawal from a member savings account and posts balanced GL entries.")
    public ResponseEntity<ApiResponse<TransactionResponse>> processWithdrawal(
            @Valid @RequestBody WithdrawalRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        UUID processedByUserId = extractCurrentUserId();
        log.info("REST Withdrawal Request: accountNo={}, amount={}, processedBy={}",
                request.getAccountNo(), request.getAmount(), processedByUserId);

        Transaction tx = cashTransactionUseCase.processWithdrawal(
                request.getAccountNo(),
                request.getAmount(),
                request.getNarration(),
                idempotencyKey,
                processedByUserId
        );

        return ResponseEntity.ok(ApiResponse.success(TransactionResponse.fromDomain(tx), "Cash withdrawal processed successfully."));
    }

    /**
     * Resolves the authenticated operator's user ID from the JWT security context.
     * Throws AccessDeniedException if the identity cannot be resolved — this prevents
     * financial transactions from being recorded with an anonymous or fabricated operator ID,
     * which would break the audit trail integrity required by Core Banking standards.
     *
     * @return UUID The verified operator user ID.
     * @throws AccessDeniedException if the operator identity cannot be determined.
     */
    private UUID extractCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof UUID uuid) {
                return uuid;
            }
            if (auth.getDetails() != null) {
                try {
                    return UUID.fromString(auth.getDetails().toString());
                } catch (Exception ignored) {}
            }
            try {
                return UUID.fromString(auth.getName());
            } catch (Exception ignored) {}
        }
        // Audit trail integrity guard: refuse the transaction rather than record it anonymously.
        throw new AccessDeniedException(
            "Operator identity could not be resolved from the authentication token. " +
            "Transaction aborted to preserve audit trail integrity."
        );
    }
}
