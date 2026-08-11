package com.kab.qershi.loan.management.infrastructure.rest;

import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.port.in.LoanDisbursementUseCase;
import com.kab.qershi.loan.management.infrastructure.rest.dto.DisburseLoanRequest;
import com.kab.qershi.loan.management.infrastructure.rest.dto.LoanAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * REST Controller for Loan Disbursement & Account Activation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loan-mgmt/disburse")
@Tag(name = "Loan Disbursement & Account Lifecycle", description = "Disburses funds for approved loan applications, activates loan accounts, and generates repayment schedules")
public class LoanDisbursementController {

    private final LoanDisbursementUseCase disbursementUseCase;

    public LoanDisbursementController(LoanDisbursementUseCase disbursementUseCase) {
        this.disbursementUseCase = disbursementUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAnyAuthority('ROLE_SACCO_ADMIN', 'ROLE_ADMIN', 'LOAN_DISBURSE:PROCESS', 'LOAN_DISBURSE_PROCESS')")
    @Operation(summary = "Disburse Loan Application", description = "Activates loan account and generates amortization schedule for an approved loan application")
    public ResponseEntity<LoanAccountResponse> disburseLoan(
            @Valid @RequestBody DisburseLoanRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        LoanDisbursementUseCase.DisburseCommand command = new LoanDisbursementUseCase.DisburseCommand(
                request.applicationId(),
                request.userId(),
                request.productId(),
                request.amount(),
                request.interestRatePct(),
                request.termMonths(),
                request.repaymentFrequency(),
                request.interestType(),
                request.targetSavingsAccountId(),
                request.memberPhone(),
                idempotencyKey
        );

        LoanAccount account = disbursementUseCase.disburseLoan(command);
        return ResponseEntity.ok(LoanAccountResponse.fromDomain(account));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAnyAuthority('LOAN_DISBURSE_APPROVE', 'LOAN_APPROVE')")
    @Operation(summary = "Maker-Checker Disbursement Approval", description = "Executes dual-control Checker approval for a loan disbursement. Rejects self-approval attempts.")
    public ResponseEntity<LoanAccountResponse> approveDisbursement(
            @PathVariable("id") UUID accountId,
            Authentication authentication) {
        UUID checkerUserId = extractCurrentUserId(authentication);
        LoanAccount account = disbursementUseCase.approveDisbursement(accountId, checkerUserId);
        return ResponseEntity.ok(LoanAccountResponse.fromDomain(account));
    }

    private UUID extractCurrentUserId(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UUID uuid) {
                return uuid;
            }
            try {
                return UUID.fromString(principal.toString());
            } catch (Exception ignored) {}
        }
        throw new AccessDeniedException("Operator identity could not be resolved for Maker-Checker approval.");
    }
}
