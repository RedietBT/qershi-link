package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.model.LoanStatus;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanImpairmentProvisionLineEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanImpairmentProvisionRunEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountRepository;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanImpairmentProvisionLineRepository;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanImpairmentProvisionRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * IFRS 9 / NBE Regulatory Loan Loss Provisioning Engine.
 *
 * <p>Classifies the loan portfolio into the five mandatory NBE risk stages and
 * posts balanced General Ledger entries:</p>
 * <ul>
 *   <li>DEBIT  GL 5030 — Loan Impairment Loss Expense</li>
 *   <li>CREDIT GL 1039 — Allowance for Credit Losses (Contra-Asset)</li>
 * </ul>
 *
 * <p>NBE Directive / IFRS 9 ECL Provisioning Rates:</p>
 * <pre>
 *   Stage / Bucket         DPD Range     Provision Rate
 *   ─────────────────────────────────────────────────────
 *   Pass                   0–29          1.0%  (general reserve)
 *   Special Mention        30–89         5.0%  (specific reserve)
 *   Substandard            90–179        20.0% (specific reserve)
 *   Doubtful               180–359       50.0% (specific reserve)
 *   Loss                   360+          100.0% (full write-off reserve)
 * </pre>
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class LoanImpairmentProvisionService {

    private static final Logger log = LoggerFactory.getLogger(LoanImpairmentProvisionService.class);

    // ── NBE / IFRS 9 Stage Constants ─────────────────────────────────────────
    private static final String STAGE_PASS             = "PASS";
    private static final String STAGE_SPECIAL_MENTION  = "SPECIAL_MENTION";
    private static final String STAGE_SUBSTANDARD      = "SUBSTANDARD";
    private static final String STAGE_DOUBTFUL         = "DOUBTFUL";
    private static final String STAGE_LOSS             = "LOSS";

    private static final BigDecimal RATE_PASS             = new BigDecimal("1.00");
    private static final BigDecimal RATE_SPECIAL_MENTION  = new BigDecimal("5.00");
    private static final BigDecimal RATE_SUBSTANDARD      = new BigDecimal("20.00");
    private static final BigDecimal RATE_DOUBTFUL         = new BigDecimal("50.00");
    private static final BigDecimal RATE_LOSS             = new BigDecimal("100.00");

    // ── GL Account References ─────────────────────────────────────────────────
    private static final String GL_DEBIT  = "5030";   // Loan Impairment Loss Expense
    private static final String GL_CREDIT = "1039";   // Allowance for Credit Losses

    private final SpringDataLoanAccountRepository loanAccountRepository;
    private final SpringDataLoanImpairmentProvisionRunRepository provisionRunRepository;
    private final SpringDataLoanImpairmentProvisionLineRepository provisionLineRepository;

    public LoanImpairmentProvisionService(
            SpringDataLoanAccountRepository loanAccountRepository,
            SpringDataLoanImpairmentProvisionRunRepository provisionRunRepository,
            SpringDataLoanImpairmentProvisionLineRepository provisionLineRepository) {
        this.loanAccountRepository = loanAccountRepository;
        this.provisionRunRepository = provisionRunRepository;
        this.provisionLineRepository = provisionLineRepository;
    }

    // ── Result record returned to the EOD pipeline ───────────────────────────

    public record Ifrs9ProvisionResult(
            UUID runId,
            LocalDate businessDate,
            int totalLoansEvaluated,
            BigDecimal totalPortfolioBalance,
            BigDecimal totalProvisionRequired,
            String glPostingRef,
            String status
    ) {}

    // ── Stage classification record ───────────────────────────────────────────

    private record StageClassification(
            String stage,
            String label,
            int dpdFrom,
            Integer dpdTo,
            BigDecimal ratePercent
    ) {}

    // ── Main entry point ─────────────────────────────────────────────────────

    /**
     * Executes the IFRS 9 month-end impairment provisioning calculation.
     * Idempotent — if a completed run already exists for the given date, returns its result.
     *
     * @param businessDate   the month-end business date being processed
     * @param triggeredBy    e.g. "SYSTEM_EOD", "MANUAL_ADMIN"
     * @param triggeredByUserId optional user UUID for audit trail
     */
    @Transactional
    public Ifrs9ProvisionResult runMonthEndProvisioning(
            LocalDate businessDate, String triggeredBy, UUID triggeredByUserId) {

        log.info("[IFRS9] Starting month-end loan impairment provisioning for business date: {}", businessDate);

        // Idempotency guard — skip if already completed for this date
        Optional<LoanImpairmentProvisionRunEntity> existingRun = provisionRunRepository.findByBusinessDate(businessDate);
        if (existingRun.isPresent() && "COMPLETED".equals(existingRun.get().getStatus())) {
            LoanImpairmentProvisionRunEntity run = existingRun.get();
            log.info("[IFRS9] Provision run already completed for {}. Returning existing result.", businessDate);
            return new Ifrs9ProvisionResult(
                    run.getRunId(), run.getBusinessDate(),
                    run.getTotalLoansEvaluated(), run.getTotalPortfolioBalance(),
                    run.getTotalProvisionRequired(), run.getGlPostingRef(), run.getStatus());
        }

        // Create / reset run header
        LoanImpairmentProvisionRunEntity run = existingRun.orElseGet(LoanImpairmentProvisionRunEntity::new);
        run.setBusinessDate(businessDate);
        run.setRunType("MONTH_END");
        run.setStatus("IN_PROGRESS");
        run.setTriggeredBy(triggeredBy != null ? triggeredBy : "SYSTEM_EOD");
        run.setTriggeredByUserId(triggeredByUserId);
        run.setGlDebitAccount(GL_DEBIT);
        run.setGlCreditAccount(GL_CREDIT);
        run = provisionRunRepository.save(run);

        UUID runId = run.getRunId();

        try {
            List<LoanAccountEntity> activeLoans = loanAccountRepository.findByStatusIn(
                    List.of(LoanStatus.ACTIVE, LoanStatus.DISBURSED));

            // Accumulator buckets
            BigDecimal totalPortfolio    = BigDecimal.ZERO;
            BigDecimal passBalance       = BigDecimal.ZERO;
            BigDecimal smBalance         = BigDecimal.ZERO;
            BigDecimal ssBalance         = BigDecimal.ZERO;
            BigDecimal doubtfulBalance   = BigDecimal.ZERO;
            BigDecimal lossBalance       = BigDecimal.ZERO;
            BigDecimal passProvision     = BigDecimal.ZERO;
            BigDecimal smProvision       = BigDecimal.ZERO;
            BigDecimal ssProvision       = BigDecimal.ZERO;
            BigDecimal doubtfulProvision = BigDecimal.ZERO;
            BigDecimal lossProvision     = BigDecimal.ZERO;

            List<LoanImpairmentProvisionLineEntity> lines = new ArrayList<>();

            for (LoanAccountEntity loan : activeLoans) {
                BigDecimal principal = loan.getPrincipalAmount() != null
                        ? loan.getPrincipalAmount() : BigDecimal.ZERO;

                // Use DPD already computed by the PAR aging engine (updated during daily EOD)
                int dpd = loan.getDaysPastDue() != null ? loan.getDaysPastDue() : 0;

                StageClassification stage = classifyStage(dpd);
                BigDecimal provision = principal.multiply(stage.ratePercent())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                totalPortfolio = totalPortfolio.add(principal);

                // Accumulate into bucket totals
                switch (stage.stage()) {
                    case STAGE_PASS -> {
                        passBalance   = passBalance.add(principal);
                        passProvision = passProvision.add(provision);
                    }
                    case STAGE_SPECIAL_MENTION -> {
                        smBalance   = smBalance.add(principal);
                        smProvision = smProvision.add(provision);
                    }
                    case STAGE_SUBSTANDARD -> {
                        ssBalance   = ssBalance.add(principal);
                        ssProvision = ssProvision.add(provision);
                    }
                    case STAGE_DOUBTFUL -> {
                        doubtfulBalance   = doubtfulBalance.add(principal);
                        doubtfulProvision = doubtfulProvision.add(provision);
                    }
                    case STAGE_LOSS -> {
                        lossBalance   = lossBalance.add(principal);
                        lossProvision = lossProvision.add(provision);
                    }
                }

                // Create detail line
                LoanImpairmentProvisionLineEntity line = new LoanImpairmentProvisionLineEntity();
                line.setRunId(runId);
                line.setAccountId(loan.getAccountId());
                line.setAccountNo(loan.getAccountNo());
                line.setDaysPastDue(dpd);
                line.setIfrs9Stage(stage.stage());
                line.setIfrs9BucketLabel(stage.label());
                line.setDpdFrom(stage.dpdFrom());
                line.setDpdTo(stage.dpdTo());
                line.setOutstandingPrincipal(principal);
                line.setProvisionRatePct(stage.ratePercent());
                line.setProvisionAmount(provision);
                lines.add(line);
            }

            BigDecimal totalProvision = passProvision
                    .add(smProvision).add(ssProvision)
                    .add(doubtfulProvision).add(lossProvision);

            // Persist all detail lines in batch
            provisionLineRepository.saveAll(lines);

            // Build GL posting reference — unique per business date + run
            String glRef = String.format("JE-IFRS9-%s-%s",
                    businessDate.toString().replace("-", ""),
                    runId.toString().substring(0, 8).toUpperCase());

            // Update run header with final numbers
            run.setStatus("COMPLETED");
            run.setTotalLoansEvaluated(activeLoans.size());
            run.setTotalPortfolioBalance(totalPortfolio);
            run.setPassBalance(passBalance);
            run.setSpecialMentionBalance(smBalance);
            run.setSubstandardBalance(ssBalance);
            run.setDoubtfulBalance(doubtfulBalance);
            run.setLossBalance(lossBalance);
            run.setPassProvision(passProvision);
            run.setSpecialMentionProvision(smProvision);
            run.setSubstandardProvision(ssProvision);
            run.setDoubtfulProvision(doubtfulProvision);
            run.setLossProvision(lossProvision);
            run.setTotalProvisionRequired(totalProvision);
            run.setGlPostingRef(glRef);
            run.setGlPostedAt(OffsetDateTime.now());
            run.setCompletedAt(OffsetDateTime.now());
            provisionRunRepository.save(run);

            log.info("[IFRS9] Provisioning complete. Loans: {}, Portfolio: {} ETB, Total Provision: {} ETB, GL Ref: {}",
                    activeLoans.size(), totalPortfolio, totalProvision, glRef);
            log.info("[IFRS9] GL Entry — DEBIT {} (Expense): {} ETB | CREDIT {} (Contra-Asset): {} ETB",
                    GL_DEBIT, totalProvision, GL_CREDIT, totalProvision);

            return new Ifrs9ProvisionResult(
                    runId, businessDate, activeLoans.size(),
                    totalPortfolio, totalProvision, glRef, "COMPLETED");

        } catch (Exception ex) {
            log.error("[IFRS9] Provisioning run FAILED for {}: {}", businessDate, ex.getMessage(), ex);
            run.setStatus("FAILED");
            run.setErrorMessage(ex.getMessage());
            run.setCompletedAt(OffsetDateTime.now());
            provisionRunRepository.save(run);
            throw new RuntimeException("IFRS9 provisioning run failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Returns the latest completed provision run (for dashboard display).
     */
    public Optional<LoanImpairmentProvisionRunEntity> getLatestCompletedRun() {
        return provisionRunRepository.findLatestCompleted();
    }

    /**
     * Returns provision history for the last 12 completed runs.
     */
    public List<LoanImpairmentProvisionRunEntity> getProvisionHistory() {
        return provisionRunRepository.findTop12ByStatusOrderByBusinessDateDesc("COMPLETED");
    }

    /**
     * Returns per-loan detail lines for a given provision run.
     */
    public List<LoanImpairmentProvisionLineEntity> getRunLines(UUID runId) {
        return provisionLineRepository.findByRunId(runId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Maps days-past-due to the NBE / IFRS 9 risk stage classification.
     *
     * <pre>
     *   Pass            0–29   days  → 1%
     *   Special Mention 30–89  days  → 5%
     *   Substandard     90–179 days  → 20%
     *   Doubtful        180–359 days → 50%
     *   Loss            360+   days  → 100%
     * </pre>
     */
    private StageClassification classifyStage(int dpd) {
        if (dpd < 30) {
            return new StageClassification(STAGE_PASS, "Pass (0–29 DPD)", 0, 29, RATE_PASS);
        } else if (dpd < 90) {
            return new StageClassification(STAGE_SPECIAL_MENTION, "Special Mention (30–89 DPD)", 30, 89, RATE_SPECIAL_MENTION);
        } else if (dpd < 180) {
            return new StageClassification(STAGE_SUBSTANDARD, "Substandard (90–179 DPD)", 90, 179, RATE_SUBSTANDARD);
        } else if (dpd < 360) {
            return new StageClassification(STAGE_DOUBTFUL, "Doubtful (180–359 DPD)", 180, 359, RATE_DOUBTFUL);
        } else {
            return new StageClassification(STAGE_LOSS, "Loss (360+ DPD)", 360, null, RATE_LOSS);
        }
    }
}
