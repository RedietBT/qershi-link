package com.kab.qershi.loan.management.infrastructure.rest;

import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.port.in.LoanScheduleUseCase;
import com.kab.qershi.loan.management.infrastructure.rest.dto.LoanAccountResponse;
import com.kab.qershi.loan.management.infrastructure.rest.dto.RepaymentScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for inspecting Loan Accounts & Amortization Repayment Schedules.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loan-mgmt/accounts")
@Tag(name = "Loan Accounts & Amortization Schedules", description = "Retrieves active loan account balances, details, and installment schedules")
public class LoanAccountController {

    private final LoanScheduleUseCase scheduleUseCase;

    public LoanAccountController(LoanScheduleUseCase scheduleUseCase) {
        this.scheduleUseCase = scheduleUseCase;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOAN_ACCOUNT:VIEW') or hasAuthority('ADMIN')")
    @Operation(summary = "Get Loan Account by ID", description = "Retrieves details of a disbursed loan account by account UUID")
    public ResponseEntity<LoanAccountResponse> getAccountById(@PathVariable("id") UUID id) {
        LoanAccount account = scheduleUseCase.getAccount(id);
        return ResponseEntity.ok(LoanAccountResponse.fromDomain(account));
    }

    @GetMapping("/{id}/schedule")
    @PreAuthorize("hasAuthority('LOAN_ACCOUNT:VIEW') or hasAuthority('ADMIN')")
    @Operation(summary = "Get Amortization Repayment Schedule", description = "Retrieves month-by-month repayment schedule installments for a loan account")
    public ResponseEntity<List<RepaymentScheduleResponse>> getAccountSchedule(@PathVariable("id") UUID id) {
        List<RepaymentSchedule> schedules = scheduleUseCase.getAccountSchedule(id);
        List<RepaymentScheduleResponse> response = schedules.stream()
                .map(RepaymentScheduleResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('LOAN_ACCOUNT:VIEW') or hasAuthority('ADMIN')")
    @Operation(summary = "Get Loan Accounts for User", description = "Lists all active and historical loan accounts for a specific borrower member")
    public ResponseEntity<List<LoanAccountResponse>> getUserAccounts(@PathVariable("userId") UUID userId) {
        List<LoanAccount> accounts = scheduleUseCase.getUserAccounts(userId);
        List<LoanAccountResponse> response = accounts.stream()
                .map(LoanAccountResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
