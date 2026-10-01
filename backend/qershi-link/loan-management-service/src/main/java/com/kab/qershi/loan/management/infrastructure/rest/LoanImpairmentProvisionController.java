package com.kab.qershi.loan.management.infrastructure.rest;

import com.kab.qershi.loan.management.application.usecase.LoanImpairmentProvisionService;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanImpairmentProvisionRunEntity;
import com.kab.qershi.loan.management.infrastructure.rest.dto.Ifrs9ProvisionRunResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API Controller for IFRS 9 / NBE Regulatory Loan Loss Provisioning.
 *
 * <p>Exposes endpoints for triggering the month-end impairment calculation,
 * fetching the latest GL posting summary, and querying provision history.</p>
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loans/ifrs9-provisioning")
@Tag(name = "IFRS 9 / NBE Loan Loss Provisioning", description = "Month-end regulatory impairment calculation and GL posting endpoints")
public class LoanImpairmentProvisionController {

    private final LoanImpairmentProvisionService provisionService;

    public LoanImpairmentProvisionController(LoanImpairmentProvisionService provisionService) {
        this.provisionService = provisionService;
    }

    // ── 1. Trigger Month-End Provisioning Run ────────────────────────────────

    @PostMapping("/run")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('LOAN_APPLICATION_APPROVE')")
    @Operation(
            summary = "Execute IFRS 9 Month-End Provisioning",
            description = "Classifies the loan portfolio into NBE risk stages, calculates ECL provisions, " +
                    "and posts a balanced GL entry: DEBIT 5030 (Loan Impairment Loss Expense) / " +
                    "CREDIT 1039 (Allowance for Credit Losses)."
    )
    public ResponseEntity<Map<String, Object>> runProvisioning(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {

        LocalDate date = (businessDate != null) ? businessDate : LocalDate.now();
        LoanImpairmentProvisionService.Ifrs9ProvisionResult result =
                provisionService.runMonthEndProvisioning(date, "MANUAL_ADMIN", null);

        return ResponseEntity.ok(Map.of(
                "runId",                  result.runId(),
                "businessDate",           result.businessDate().toString(),
                "status",                 result.status(),
                "totalLoansEvaluated",    result.totalLoansEvaluated(),
                "totalPortfolioBalance",  result.totalPortfolioBalance(),
                "totalProvisionRequired", result.totalProvisionRequired(),
                "glPostingRef",           result.glPostingRef(),
                "glEntry", Map.of(
                        "debit",  "GL 5030 — Loan Impairment Loss Expense",
                        "credit", "GL 1039 — Allowance for Credit Losses",
                        "amount", result.totalProvisionRequired()
                )
        ));
    }

    // ── 2. Get Latest Completed Provision Run ────────────────────────────────

    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('LOAN_DELINQUENCY_VIEW', 'LOAN_ACCOUNT_VIEW')")
    @Operation(
            summary = "Get Latest IFRS 9 Provision Run",
            description = "Returns the most recent completed month-end impairment provision run with full GL posting details."
    )
    public ResponseEntity<Ifrs9ProvisionRunResponse> getLatestProvisionRun() {
        return provisionService.getLatestCompletedRun()
                .map(run -> ResponseEntity.ok(toResponse(run)))
                .orElse(ResponseEntity.noContent().build());
    }

    // ── 3. Get Provision Run History ─────────────────────────────────────────

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('LOAN_DELINQUENCY_VIEW', 'LOAN_ACCOUNT_VIEW')")
    @Operation(
            summary = "Get IFRS 9 Provision History",
            description = "Returns the last 12 completed month-end impairment provision runs for trend reporting."
    )
    public ResponseEntity<List<Ifrs9ProvisionRunResponse>> getProvisionHistory() {
        List<Ifrs9ProvisionRunResponse> history = provisionService.getProvisionHistory()
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(history);
    }

    // ── 4. Get Per-Loan Lines for a Run ─────────────────────────────────────

    @GetMapping("/{runId}/lines")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('LOAN_DELINQUENCY_VIEW')")
    @Operation(
            summary = "Get IFRS 9 Per-Loan Provision Lines",
            description = "Returns the per-loan ECL provision detail lines for a given provision run (for regulatory reporting)."
    )
    public ResponseEntity<List<Map<String, Object>>> getRunLines(@PathVariable UUID runId) {
        List<Map<String, Object>> lines = provisionService.getRunLines(runId).stream()
                .map(line -> Map.<String, Object>of(
                        "lineId",               line.getLineId(),
                        "accountId",            line.getAccountId(),
                        "accountNo",            line.getAccountNo() != null ? line.getAccountNo() : "",
                        "daysPastDue",          line.getDaysPastDue(),
                        "ifrs9Stage",           line.getIfrs9Stage(),
                        "ifrs9BucketLabel",     line.getIfrs9BucketLabel(),
                        "dpdRange",             line.getDpdFrom() + "–" + (line.getDpdTo() != null ? line.getDpdTo() : "∞"),
                        "outstandingPrincipal", line.getOutstandingPrincipal(),
                        "provisionRatePct",     line.getProvisionRatePct(),
                        "provisionAmount",      line.getProvisionAmount()
                ))
                .toList();
        return ResponseEntity.ok(lines);
    }

    // ── Private mapper ───────────────────────────────────────────────────────

    private Ifrs9ProvisionRunResponse toResponse(LoanImpairmentProvisionRunEntity run) {
        return new Ifrs9ProvisionRunResponse(
                run.getRunId(),
                run.getBusinessDate(),
                run.getRunType(),
                run.getStatus(),
                run.getTotalLoansEvaluated(),
                run.getTotalPortfolioBalance(),
                run.getPassBalance(),
                run.getSpecialMentionBalance(),
                run.getSubstandardBalance(),
                run.getDoubtfulBalance(),
                run.getLossBalance(),
                run.getPassProvision(),
                run.getSpecialMentionProvision(),
                run.getSubstandardProvision(),
                run.getDoubtfulProvision(),
                run.getLossProvision(),
                run.getTotalProvisionRequired(),
                run.getGlDebitAccount(),
                run.getGlCreditAccount(),
                run.getGlPostingRef(),
                run.getGlPostedAt(),
                run.getTriggeredBy(),
                run.getCompletedAt()
        );
    }
}
