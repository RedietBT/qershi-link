package com.kab.qershi.loan.management.infrastructure.rest;

import com.kab.qershi.loan.management.application.usecase.LoanDelinquencyService;
import com.kab.qershi.loan.management.domain.model.LoanStatus;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanDelinquencySnapshotEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountRepository;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanDelinquencySnapshotRepository;
import com.kab.qershi.loan.management.infrastructure.rest.dto.DelinquentLoanResponse;
import com.kab.qershi.loan.management.infrastructure.rest.dto.ParSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API Controller for Portfolio at Risk (PAR) and Delinquency Analysis.
 * Provides regulatory provisioning summaries, aging buckets, and loan portfolio risk inspection.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loans/delinquency")
@Tag(name = "Loan Delinquency & PAR Engine", description = "Endpoints for Portfolio at Risk aging, provisioning, and delinquency monitoring")
public class LoanDelinquencyController {

    private final LoanDelinquencyService delinquencyService;
    private final SpringDataLoanAccountRepository loanAccountRepository;
    private final SpringDataLoanDelinquencySnapshotRepository snapshotRepository;

    public LoanDelinquencyController(LoanDelinquencyService delinquencyService,
                                     SpringDataLoanAccountRepository loanAccountRepository,
                                     SpringDataLoanDelinquencySnapshotRepository snapshotRepository) {
        this.delinquencyService = delinquencyService;
        this.loanAccountRepository = loanAccountRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate Portfolio Delinquency", description = "Executes the PAR aging engine for all active loans as of the specified business date.")
    public ResponseEntity<Map<String, Object>> evaluateDelinquency(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate) {
        LocalDate date = (businessDate != null) ? businessDate : LocalDate.now();
        LoanDelinquencyService.ParAgingResult result = delinquencyService.evaluateParAging(date);

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
        List<LoanAccountEntity> loans = loanAccountRepository.findByStatusIn(
                List.of(LoanStatus.ACTIVE, LoanStatus.DISBURSED)
        );

        BigDecimal totalPrincipal = BigDecimal.ZERO;
        int currentCount = 0;
        BigDecimal currentAmount = BigDecimal.ZERO;
        int par30Count = 0;
        BigDecimal par30Amount = BigDecimal.ZERO;
        int par60Count = 0;
        BigDecimal par60Amount = BigDecimal.ZERO;
        int par90Count = 0;
        BigDecimal par90Amount = BigDecimal.ZERO;
        int lossCount = 0;
        BigDecimal lossAmount = BigDecimal.ZERO;
        BigDecimal totalProvisions = BigDecimal.ZERO;

        for (LoanAccountEntity loan : loans) {
            BigDecimal principal = loan.getPrincipalAmount() != null ? loan.getPrincipalAmount() : BigDecimal.ZERO;
            totalPrincipal = totalPrincipal.add(principal);

            BigDecimal provision = loan.getProvisionAmount() != null ? loan.getProvisionAmount() : BigDecimal.ZERO;
            totalProvisions = totalProvisions.add(provision);

            String bucket = loan.getParBucket() != null ? loan.getParBucket() : "CURRENT";
            switch (bucket) {
                case "WATCHLIST_PAR_30" -> {
                    par30Count++;
                    par30Amount = par30Amount.add(principal);
                }
                case "SUBSTANDARD_PAR_60" -> {
                    par60Count++;
                    par60Amount = par60Amount.add(principal);
                }
                case "DOUBTFUL_PAR_90" -> {
                    par90Count++;
                    par90Amount = par90Amount.add(principal);
                }
                case "LOSS_PAR_90_PLUS" -> {
                    lossCount++;
                    lossAmount = lossAmount.add(principal);
                }
                default -> {
                    currentCount++;
                    currentAmount = currentAmount.add(principal);
                }
            }
        }

        BigDecimal nplRatio = BigDecimal.ZERO;
        if (totalPrincipal.compareTo(BigDecimal.ZERO) > 0) {
            nplRatio = lossAmount.multiply(new BigDecimal("100")).divide(totalPrincipal, 2, RoundingMode.HALF_UP);
        }

        ParSummaryResponse response = new ParSummaryResponse(
                loans.size(),
                totalPrincipal,
                currentCount,
                currentAmount,
                par30Count,
                par30Amount,
                par60Count,
                par60Amount,
                par90Count,
                par90Amount,
                lossCount,
                lossAmount,
                nplRatio,
                totalProvisions
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/loans")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('LOAN_DELINQUENCY_VIEW', 'LOAN_ACCOUNT_VIEW')")
    @Operation(summary = "List Delinquent Loans", description = "Retrieves overdue loan accounts categorized by regulatory PAR aging buckets.")
    public ResponseEntity<List<DelinquentLoanResponse>> getDelinquentLoans(
            @RequestParam(required = false) String bucket) {

        List<LoanDelinquencySnapshotEntity> snapshots = (bucket != null && !bucket.isBlank())
                ? snapshotRepository.findLatestSnapshots().stream()
                    .filter(s -> bucket.equalsIgnoreCase(s.getParBucket()))
                    .toList()
                : snapshotRepository.findLatestSnapshots();

        Map<UUID, LoanAccountEntity> loanMap = loanAccountRepository.findAll().stream()
                .collect(Collectors.toMap(LoanAccountEntity::getAccountId, l -> l, (l1, l2) -> l1));

        List<DelinquentLoanResponse> result = new ArrayList<>();
        for (LoanDelinquencySnapshotEntity s : snapshots) {
            LoanAccountEntity loan = loanMap.get(s.getAccountId());
            result.add(new DelinquentLoanResponse(
                    s.getSnapshotId(),
                    s.getAccountId(),
                    loan != null ? loan.getAccountNo() : "ACC-" + s.getAccountId().toString().substring(0, 8),
                    loan != null ? loan.getUserId() : null,
                    loan != null ? loan.getPrincipalAmount() : BigDecimal.ZERO,
                    s.getDaysPastDue(),
                    s.getOverduePrincipal(),
                    s.getOverdueInterest(),
                    s.getTotalOverdue(),
                    s.getParBucket(),
                    s.getProvisionRatePct(),
                    s.getProvisionAmount(),
                    s.getBusinessDate()
            ));
        }

        return ResponseEntity.ok(result);
    }
}
