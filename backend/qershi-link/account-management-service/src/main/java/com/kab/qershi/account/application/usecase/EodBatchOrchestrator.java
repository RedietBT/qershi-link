package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.infrastructure.persistence.EodBatchExecutionEntity;
import com.kab.qershi.account.infrastructure.persistence.EodBatchStepLogEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataEodBatchExecutionRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataEodBatchStepLogRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataSystemBusinessDateRepository;
import com.kab.qershi.account.infrastructure.persistence.SystemBusinessDateEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core Banking End-of-Day (EOD) Batch Pipeline Orchestrator.
 * Coordinates system cutoff lock, savings interest accrual, dormancy sweeps,
 * loan portfolio-at-risk (PAR) aging, and business date rollover.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class EodBatchOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(EodBatchOrchestrator.class);

    private final SpringDataSystemBusinessDateRepository businessDateRepository;
    private final SpringDataEodBatchExecutionRepository batchExecutionRepository;
    private final SpringDataEodBatchStepLogRepository stepLogRepository;
    private final InterestAccrualService interestAccrualService;
    private final AccountDormancyService accountDormancyService;
    private final TermDepositService termDepositService;
    private final RestTemplate restTemplate;

    @Value("${services.loan-management.url:http://loan-management-service:8085}")
    private String loanServiceUrl;

    public EodBatchOrchestrator(SpringDataSystemBusinessDateRepository businessDateRepository,
                                SpringDataEodBatchExecutionRepository batchExecutionRepository,
                                SpringDataEodBatchStepLogRepository stepLogRepository,
                                InterestAccrualService interestAccrualService,
                                AccountDormancyService accountDormancyService,
                                TermDepositService termDepositService,
                                RestTemplateBuilder restTemplateBuilder) {
        this.businessDateRepository = businessDateRepository;
        this.batchExecutionRepository = batchExecutionRepository;
        this.stepLogRepository = stepLogRepository;
        this.interestAccrualService = interestAccrualService;
        this.accountDormancyService = accountDormancyService;
        this.termDepositService = termDepositService;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    public SystemBusinessDateEntity getOrCreateCurrentBusinessDate() {
        return businessDateRepository.findCurrentBusinessDate().orElseGet(() -> {
            LocalDate today = LocalDate.now();
            boolean isMonthEnd = today.plusDays(1).getMonth() != today.getMonth();
            SystemBusinessDateEntity initialDate = new SystemBusinessDateEntity(
                    UUID.randomUUID(),
                    today,
                    "OPEN",
                    isMonthEnd,
                    null,
                    null,
                    LocalDateTime.now()
            );
            return businessDateRepository.save(initialDate);
        });
    }

    @Transactional
    public EodBatchExecutionEntity runEodBatch(String triggeredBy, UUID triggeredByUserId) {
        SystemBusinessDateEntity businessDateEntity = getOrCreateCurrentBusinessDate();
        LocalDate businessDate = businessDateEntity.getCurrentBusinessDate();
        boolean isMonthEnd = Boolean.TRUE.equals(businessDateEntity.getIsMonthEnd());

        log.info("Initiating Core Banking EOD batch execution for business date: {} (Trigger: {})",
                businessDate, triggeredBy);

        // 1. Initial State Check & Lock
        if ("PROCESSING_EOD".equals(businessDateEntity.getStatus())) {
            throw new IllegalStateException("An EOD batch run is already currently in progress.");
        }

        businessDateEntity.setStatus("PROCESSING_EOD");
        businessDateEntity.setUpdatedAt(LocalDateTime.now());
        businessDateRepository.save(businessDateEntity);

        EodBatchExecutionEntity execution = new EodBatchExecutionEntity();
        execution.setBusinessDate(businessDate);
        execution.setStartedAt(LocalDateTime.now());
        execution.setStatus("IN_PROGRESS");
        execution.setTriggeredBy(triggeredBy != null ? triggeredBy : "MANUAL_OVERRIDE");
        execution.setTriggeredByUserId(triggeredByUserId);
        execution = batchExecutionRepository.save(execution);

        UUID batchId = execution.getBatchId();

        try {
            // STEP 1: CUTOFF LOCK
            executeStep(batchId, "CUTOFF_LOCK", () -> {
                log.info("Daytime transaction postings locked during EOD cutoff.");
                return 1;
            });

            // STEP 2: SAVINGS DAILY INTEREST ACCRUAL & MONTH-END CAPITALIZATION
            final EodBatchExecutionEntity currentExec = execution;
            executeStep(batchId, "SAVINGS_INTEREST_ACCRUAL", () -> {
                InterestAccrualService.AccrualResult accrualResult =
                        interestAccrualService.runDailyAccrual(businessDate, isMonthEnd);
                currentExec.setTotalAccountsAccrued(accrualResult.accountsAccrued());
                currentExec.setTotalInterestAccrued(accrualResult.totalInterestAccrued());
                return accrualResult.accountsAccrued();
            });

            // STEP 3: ACCOUNT DORMANCY SWEEP (> 180 Days Inactivity)
            executeStep(batchId, "DORMANCY_SWEEP", () -> {
                int dormantCount = accountDormancyService.sweepDormantAccounts(businessDate);
                currentExec.setTotalAccountsDormant(dormantCount);
                return dormantCount;
            });

            // STEP 3b: FIXED TERM DEPOSIT DAILY ACCRUAL + MATURITY SWEEP
            executeStep(batchId, "TERM_DEPOSIT_ACCRUAL", () -> {
                int fdAccrued = termDepositService.runDailyFdAccrual(businessDate);
                return fdAccrued;
            });
            executeStep(batchId, "TERM_DEPOSIT_MATURITY", () -> {
                TermDepositService.MaturityProcessResult fdResult =
                        termDepositService.processMaturedContracts(businessDate);
                return fdResult.contractsProcessed();
            });

            // STEP 4: LOAN PORTFOLIO AT RISK (PAR) AGING
            executeStep(batchId, "LOAN_PAR_AGING", () -> {
                int loansEvaluated = triggerLoanParAging(businessDate);
                currentExec.setTotalLoansEvaluated(loansEvaluated);
                return loansEvaluated;
            });

            // STEP 5: IFRS 9 / NBE REGULATORY LOAN LOSS PROVISIONING (Month-End Only)
            if (isMonthEnd) {
                executeStep(batchId, "IFRS9_LOAN_LOSS_PROVISIONING", () -> {
                    int loansProvisioned = triggerIfrs9Provisioning(businessDate);
                    currentExec.setTotalLoansProvisioned(loansProvisioned);
                    return loansProvisioned;
                });
            } else {
                log.info("[EOD] Skipping IFRS9_LOAN_LOSS_PROVISIONING — not a month-end date.");
            }

            // STEP 6: BUSINESS DATE ROLLOVER
            LocalDate nextBusinessDate = businessDate.plusDays(1);
            boolean nextMonthEnd = nextBusinessDate.plusDays(1).getMonth() != nextBusinessDate.getMonth();

            executeStep(batchId, "DATE_ROLLOVER", () -> {
                businessDateEntity.setCurrentBusinessDate(nextBusinessDate);
                businessDateEntity.setIsMonthEnd(nextMonthEnd);
                businessDateEntity.setStatus("OPEN");
                businessDateEntity.setLastEodCompletedAt(LocalDateTime.now());
                businessDateEntity.setUpdatedAt(LocalDateTime.now());
                businessDateRepository.save(businessDateEntity);
                log.info("Rolled business date from {} to {}. Transaction posting unlocked.", businessDate, nextBusinessDate);
                return 1;
            });

            execution.setStatus("COMPLETED");
            execution.setCompletedAt(LocalDateTime.now());
            execution.setSummaryNotes(String.format(
                    "Batch completed successfully. Processed %d interest accruals, %d dormant accounts, " +
                    "%d loans PAR-aged, %d loans IFRS9-provisioned (month-end=%b). Rolled date to %s.",
                    execution.getTotalAccountsAccrued(), execution.getTotalAccountsDormant(),
                    execution.getTotalLoansEvaluated(),
                    execution.getTotalLoansProvisioned() != null ? execution.getTotalLoansProvisioned() : 0,
                    isMonthEnd, nextBusinessDate));
            return batchExecutionRepository.save(execution);

        } catch (Exception ex) {
            log.error("Fatal error during EOD batch execution: {}", ex.getMessage(), ex);
            businessDateEntity.setStatus("OPEN");
            businessDateEntity.setUpdatedAt(LocalDateTime.now());
            businessDateRepository.save(businessDateEntity);

            execution.setStatus("FAILED");
            execution.setCompletedAt(LocalDateTime.now());
            execution.setSummaryNotes("Batch failed: " + ex.getMessage());
            batchExecutionRepository.save(execution);

            throw new RuntimeException("EOD Batch Execution Failed: " + ex.getMessage(), ex);
        }
    }

    private int triggerLoanParAging(LocalDate businessDate) {
        try {
            String url = loanServiceUrl + "/api/v1/loans/delinquency/evaluate?businessDate=" + businessDate;
            log.info("Calling Loan Service PAR aging at: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object totalEvaluated = response.getBody().get("totalLoansEvaluated");
                if (totalEvaluated instanceof Number num) {
                    return num.intValue();
                }
            }
            return 0;
        } catch (Exception e) {
            log.warn("Loan service PAR aging call failed or service unreachable: {}. Step recorded with 0 loans.", e.getMessage());
            return 0;
        }
    }

    /**
     * Calls the Loan Management Service IFRS 9 provisioning endpoint.
     * Only invoked on month-end business dates.
     *
     * @param businessDate the month-end date being processed
     * @return number of loans provisioned, or 0 on failure (non-fatal)
     */
    private int triggerIfrs9Provisioning(LocalDate businessDate) {
        try {
            String url = loanServiceUrl + "/api/v1/loans/ifrs9-provisioning/run?businessDate=" + businessDate;
            log.info("[EOD-IFRS9] Triggering IFRS 9 month-end provisioning at: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object total = response.getBody().get("totalLoansEvaluated");
                if (total instanceof Number num) {
                    log.info("[EOD-IFRS9] Provisioning completed. Loans provisioned: {}. GL Ref: {}",
                            num.intValue(), response.getBody().get("glPostingRef"));
                    return num.intValue();
                }
            }
            return 0;
        } catch (Exception e) {
            log.warn("[EOD-IFRS9] IFRS 9 provisioning call failed or service unreachable: {}. Step recorded with 0 loans.",
                    e.getMessage());
            return 0;
        }
    }

    @FunctionalInterface
    private interface StepAction {
        int execute() throws Exception;
    }

    private void executeStep(UUID batchId, String stepName, StepAction action) {
        long startTime = System.currentTimeMillis();
        try {
            int recordsAffected = action.execute();
            long duration = System.currentTimeMillis() - startTime;
            stepLogRepository.save(new EodBatchStepLogEntity(
                    batchId, stepName, "SUCCESS", duration, recordsAffected, null
            ));
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            stepLogRepository.save(new EodBatchStepLogEntity(
                    batchId, stepName, "FAILED", duration, 0, e.getMessage()
            ));
            throw new RuntimeException("Step " + stepName + " failed: " + e.getMessage(), e);
        }
    }
}
