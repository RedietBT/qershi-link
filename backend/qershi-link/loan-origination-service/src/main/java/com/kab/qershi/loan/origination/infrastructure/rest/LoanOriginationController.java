package com.kab.qershi.loan.origination.infrastructure.rest;

import com.kab.qershi.loan.origination.domain.model.LoanApplication;
import com.kab.qershi.loan.origination.domain.ports.inbound.ApprovalWorkflowUseCase;
import com.kab.qershi.loan.origination.domain.ports.inbound.ApprovalWorkflowUseCase.ProcessApprovalCommand;
import com.kab.qershi.loan.origination.domain.ports.inbound.LoanApplicationUseCase;
import com.kab.qershi.loan.origination.domain.ports.inbound.LoanApplicationUseCase.SubmitApplicationCommand;
import com.kab.qershi.loan.origination.infrastructure.rest.dto.ApprovalRequest;
import com.kab.qershi.loan.origination.infrastructure.rest.dto.LoanApplicationRequest;
import com.kab.qershi.loan.origination.infrastructure.rest.dto.LoanApplicationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller managing loan application submission, lookup, and Maker-Checker approval.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loan-org")
@Tag(name = "Loan Origination & Maker-Checker Approval", description = "Endpoints for submitting loan applications and processing dual-control approvals")
public class LoanOriginationController {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final ApprovalWorkflowUseCase approvalWorkflowUseCase;

    public LoanOriginationController(LoanApplicationUseCase loanApplicationUseCase,
                                     ApprovalWorkflowUseCase approvalWorkflowUseCase) {
        this.loanApplicationUseCase = loanApplicationUseCase;
        this.approvalWorkflowUseCase = approvalWorkflowUseCase;
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyAuthority('ROLE_SACCO_ADMIN', 'ROLE_SUPER_ADMIN', 'LOAN_APPLICATION_CREATE', 'LOAN_REQUEST_CREATE')")
    @Operation(summary = "Submit Loan Application", description = "Submits individual or group loan application and triggers automated multi-factor credit scoring")
    public ResponseEntity<LoanApplicationResponse> submitApplication(@Valid @RequestBody LoanApplicationRequest request) {
        SubmitApplicationCommand command = new SubmitApplicationCommand(
                request.userId(),
                request.groupId(),
                request.productId(),
                request.scoringType(),
                request.amountRequested(),
                request.savingsConsistency(),
                request.historicalYield(),
                request.projectedYield(),
                request.landSizeHectares(),
                request.collaterals()
        );

        LoanApplication application = loanApplicationUseCase.submitApplication(command);
        return new ResponseEntity<>(LoanApplicationResponse.fromDomain(application), HttpStatus.CREATED);
    }

    @GetMapping("/applications/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SACCO_ADMIN', 'ROLE_SUPER_ADMIN', 'LOAN_APPLICATION_VIEW')")
    @Operation(summary = "Get Application by ID", description = "Retrieves loan application, credit scoring, collateral, and approval audit trail")
    public ResponseEntity<LoanApplicationResponse> getApplicationById(@PathVariable("id") UUID applicationId) {
        LoanApplication application = loanApplicationUseCase.getApplicationById(applicationId);
        return ResponseEntity.ok(LoanApplicationResponse.fromDomain(application));
    }

    @GetMapping("/applications/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SACCO_ADMIN', 'ROLE_SUPER_ADMIN', 'LOAN_APPLICATION_VIEW')")
    @Operation(summary = "List User Applications", description = "Retrieves all loan applications submitted by a specific borrower user ID")
    public ResponseEntity<List<LoanApplicationResponse>> listApplicationsForUser(@PathVariable("userId") UUID userId) {
        List<LoanApplicationResponse> response = loanApplicationUseCase.listApplicationsForUser(userId).stream()
                .map(LoanApplicationResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/approve/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SACCO_ADMIN', 'ROLE_SUPER_ADMIN', 'LOAN_APPLICATION_APPROVE', 'LOAN_APPROVE')")
    @Operation(summary = "Maker-Checker Final Approval", description = "Executes dual-control decision (APPROVE/REJECT). Rejects self-approval attempts by applicant (403 Forbidden).")
    public ResponseEntity<LoanApplicationResponse> processApproval(@PathVariable("id") UUID applicationId,
                                                                   @Valid @RequestBody ApprovalRequest request,
                                                                   Authentication authentication) {
        // Resolve checker userId from authenticated principal name or subject
        UUID checkerUserId = parseUserIdFromAuthentication(authentication);

        ProcessApprovalCommand command = new ProcessApprovalCommand(
                applicationId,
                checkerUserId,
                request.actionType(),
                request.amountApproved(),
                request.remarks()
        );

        LoanApplication application = approvalWorkflowUseCase.processApproval(command);
        return ResponseEntity.ok(LoanApplicationResponse.fromDomain(application));
    }

    private UUID parseUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            // Fallback UUID if principal is phone number or username
            return UUID.nameUUIDFromBytes(authentication.getName().getBytes());
        }
    }
}
