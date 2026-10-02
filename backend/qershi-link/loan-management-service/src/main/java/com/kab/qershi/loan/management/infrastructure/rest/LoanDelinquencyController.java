package com.kab.qershi.loan.management.infrastructure.rest;

import com.kab.qershi.loan.management.domain.model.ParAgingResult;
import com.kab.qershi.loan.management.domain.model.ParSummary;
import com.kab.qershi.loan.management.domain.port.in.LoanDelinquencyUseCase;
import com.kab.qershi.loan.management.infrastructure.rest.dto.DelinquentLoanResponse;
import com.kab.qershi.loan.management.infrastructure.rest.dto.ParSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Portfolio at Risk (PAR) and Delinquency Analysis.
 * Follows strict Hexagonal Architecture DDD principles.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loans/delinquency")
@Tag(name = "Loan Delinquency & PAR Engine", description = "Endpoints for Portfolio at Risk aging, provisioning, and delinquency monitoring")
public class LoanDelinquencyController {

    private final LoanDelinquencyUseCase delinquencyUseCase;

    public LoanDelinquencyController(LoanDelinquencyUseCase delinquencyUseCase) {
        this.delinquencyUseCase = delinquencyUseCase;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate Portfolio Delinquency", description = "Executes the PAR aging engine for all active loans as of the specified business date.")
    public ResponseEntity<Map<String, Object>> evaluateDelinquency(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        LocalDate date = (businessDate != null) ? businessDate : LocalDate.now();
        ParAgingResult result = delinquencyUseCase.evaluateParAging(date);

        return ResponseEntity.ok(Map.of(
                "businessDate", date.toString(),
                "totalLoansEvaluated", result.totalLoansEvaluated(),
                "currentCount", result.currentCount(),
                "par30Count", result.par30Count(),
                "par60Count", result.par60Count(),
                "par90Count", result.par90Count(),
                "lossCount", result.lossCount(),
                "totalOverdueAmount", result.totalOverdueAmount(),
                "totalProvisionReserve", result.totalProvisionReserve()
        ));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('LOAN_DELINQUENCY_VIEW', 'LOAN_ACCOUNT_VIEW')")
    @Operation(summary = "Get PAR Summary & Provisioning Reserves", description = "Fetches aggregate portfolio health, delinquency breakdown by PAR bucket, and required regulatory loan loss reserve reserves.")
    public ResponseEntity<ParSummaryResponse> getParSummary() {
        ParSummary summary = delinquencyUseCase.getParSummary();
        return ResponseEntity.ok(ParSummaryResponse.fromDomain(summary));
    }

    @GetMapping("/loans")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('LOAN_DELINQUENCY_VIEW', 'LOAN_ACCOUNT_VIEW')")
    @Operation(summary = "List Delinquent Loans", description = "Retrieves overdue loan accounts categorized by regulatory PAR aging buckets.")
    public ResponseEntity<List<DelinquentLoanResponse>> getDelinquentLoans(
            @RequestParam(required = false) String bucket) {
        List<DelinquentLoanResponse> response = delinquencyUseCase.getDelinquentLoans(bucket).stream()
                .map(DelinquentLoanResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }
}
