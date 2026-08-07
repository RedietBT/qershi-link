package com.kab.qershi.loan.management.infrastructure.rest;

import com.kab.qershi.loan.management.domain.model.LoanRepayment;
import com.kab.qershi.loan.management.domain.port.in.LoanRepaymentUseCase;
import com.kab.qershi.loan.management.infrastructure.rest.dto.LoanRepaymentResponse;
import com.kab.qershi.loan.management.infrastructure.rest.dto.ProcessRepaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Loan Repayment Processing.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loan-mgmt/repayments")
@Tag(name = "Loan Repayment Processing", description = "Processes loan repayments using Payment Waterfall rules (Penalties ➔ Interest ➔ Principal)")
public class LoanRepaymentController {

    private final LoanRepaymentUseCase repaymentUseCase;

    public LoanRepaymentController(LoanRepaymentUseCase repaymentUseCase) {
        this.repaymentUseCase = repaymentUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LOAN_REPAYMENT:PROCESS') or hasAuthority('ADMIN')")
    @Operation(summary = "Process Loan Repayment", description = "Executes repayment allocation using payment waterfall and updates schedule statuses")
    public ResponseEntity<LoanRepaymentResponse> processRepayment(@Valid @RequestBody ProcessRepaymentRequest request) {
        LoanRepaymentUseCase.RepaymentCommand command = new LoanRepaymentUseCase.RepaymentCommand(
                request.accountId(),
                request.amountPaid(),
                request.paymentChannel(),
                request.remarks()
        );

        LoanRepayment repayment = repaymentUseCase.processRepayment(command);
        return ResponseEntity.ok(LoanRepaymentResponse.fromDomain(repayment));
    }
}
