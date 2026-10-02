package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.TermDepositContract;
import com.kab.qershi.account.domain.model.TermDepositStatus;
import com.kab.qershi.account.domain.ports.inbound.TermDepositUseCase;
import com.kab.qershi.account.domain.ports.outbound.TermDepositRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pure Hexagonal Application Service for Fixed Term Deposit (FD) contracts.
 * Operates purely on Domain Models and Ports (zero direct persistence coupling).
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@Service
public class TermDepositService implements TermDepositUseCase {

    private static final Logger log = LoggerFactory.getLogger(TermDepositService.class);

    private final TermDepositRepositoryPort termDepositRepository;

    public TermDepositService(TermDepositRepositoryPort termDepositRepository) {
        this.termDepositRepository = termDepositRepository;
    }

    // ── 1. OPEN Term Deposit Contract ─────────────────────────────────────────

    @Override
    @Transactional
    public OpenFdResult openTermDeposit(
            String accountNo, UUID userId, String saccoCode, String branchCode,
            BigDecimal principalAmount, int tenorMonths, BigDecimal agreedRatePa,
            boolean autoRollover, Integer rolloverTenorMonths,
            UUID makerUserId, String makerNotes, boolean autoApprove) {

        return openTermDeposit(
                accountNo, userId, saccoCode, branchCode,
                principalAmount, tenorMonths, agreedRatePa,
                new BigDecimal("2.00"), autoRollover, rolloverTenorMonths,
                makerUserId, makerNotes
        );
    }

    @Transactional
    public OpenFdResult openTermDeposit(
            String accountNo, UUID userId, String saccoCode, String branchCode,
            BigDecimal principalAmount, int tenorMonths, BigDecimal agreedRatePa,
            BigDecimal earlyBreakPenaltyPct, boolean autoRollover, Integer rolloverTenorMonths,
            UUID makerUserId, String makerNotes) {

        if (principalAmount == null || principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be positive.");
        }
        if (tenorMonths < 1) {
            throw new IllegalArgumentException("Tenor must be at least 1 month.");
        }
        if (agreedRatePa == null || agreedRatePa.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Interest rate must be positive.");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate maturityDate = startDate.plusMonths(tenorMonths);

        String contractNo = "FD-" + startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String glRef = "JE-FD-OPEN-" + contractNo;

        TermDepositContract contract = new TermDepositContract(
                UUID.randomUUID(),
                contractNo,
                accountNo,
                userId,
                saccoCode,
                branchCode,
                principalAmount,
                tenorMonths,
                agreedRatePa,
                earlyBreakPenaltyPct != null ? earlyBreakPenaltyPct : new BigDecimal("2.00"),
                startDate,
                maturityDate,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                TermDepositStatus.ACTIVE,
                autoRollover,
                rolloverTenorMonths,
                null,
                null,
                "2060",
                "1010",
                glRef,
                null,
                makerUserId,
                makerNotes,
                null,
                null,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        TermDepositContract saved = termDepositRepository.save(contract);

        log.info("[FD] Opened contract {} for account {} | Principal: {} ETB | Tenor: {}M | Rate: {}% p.a. | Maturity: {} | GL Ref: {}",
                contractNo, accountNo, principalAmount, tenorMonths, agreedRatePa, maturityDate, glRef);
        log.info("[FD] GL Entry — DEBIT GL 1010 (Savings): {} ETB → CREDIT GL 2060 (FD Liability): {} ETB",
                principalAmount, principalAmount);

        return new OpenFdResult(
                saved.getContractId(), contractNo, maturityDate,
                principalAmount, agreedRatePa, glRef, "ACTIVE");
    }

    // ── 2. DAILY EOD INTEREST ACCRUAL ─────────────────────────────────────────

    @Override
    @Transactional
    public DailyAccrualResult runDailyInterestAccrual(LocalDate businessDate) {
        int count = runDailyFdAccrual(businessDate);
        return new DailyAccrualResult(count, BigDecimal.ZERO, businessDate);
    }

    @Transactional
    public int runDailyFdAccrual(LocalDate businessDate) {
        List<TermDepositContract> activeContracts =
                termDepositRepository.findActiveContractsForAccrual(businessDate);

        int count = 0;
        BigDecimal totalAccrued = BigDecimal.ZERO;

        for (TermDepositContract contract : activeContracts) {
            BigDecimal dailyInterest = contract.computeDailyInterest();
            contract.accrueDailyInterest(dailyInterest);
            termDepositRepository.save(contract);
            totalAccrued = totalAccrued.add(dailyInterest);
            count++;
        }

        if (count > 0) {
            log.info("[FD-ACCRUAL] {} FD contracts accrued on {}. Total daily interest: {} ETB | GL: DEBIT 2055 / CREDIT 2051",
                    count, businessDate, totalAccrued);
        }
        return count;
    }

    // ── 3. MATURITY PROCESSING ────────────────────────────────────────────────

    @Override
    @Transactional
    public MaturityProcessResult processMaturitySweep(LocalDate businessDate) {
        return processMaturedContracts(businessDate);
    }

    @Transactional
    public MaturityProcessResult processMaturedContracts(LocalDate businessDate) {
        List<TermDepositContract> matured = termDepositRepository.findMaturedContracts(businessDate);

        int rolledOver = 0;
        int closedNormal = 0;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;

        for (TermDepositContract contract : matured) {
            BigDecimal principal = contract.getPrincipalAmount();
            BigDecimal interest = contract.getAccruedInterest();
            BigDecimal totalPayout = principal.add(interest);
            String closingGlRef = "JE-FD-MAT-" + contract.getContractNo();

            if (Boolean.TRUE.equals(contract.getAutoRollover())) {
                int newTenor = contract.getRolloverTenorMonths() != null
                        ? contract.getRolloverTenorMonths()
                        : contract.getTenorMonths();

                contract.markRolledOver(closingGlRef, businessDate);
                termDepositRepository.save(contract);

                openTermDeposit(
                        contract.getAccountNo(), contract.getUserId(),
                        contract.getSaccoCode(), contract.getBranchCode(),
                        principal, newTenor, contract.getAgreedInterestRatePa(),
                        contract.getEarlyBreakPenaltyPct(), contract.getAutoRollover(),
                        contract.getRolloverTenorMonths(),
                        contract.getMakerUserId(), "Auto-rollover from " + contract.getContractNo());

                rolledOver++;
                log.info("[FD-MATURITY] Contract {} auto-rolled over. Interest credited: {} ETB", contract.getContractNo(), interest);
            } else {
                contract.closeNormal(closingGlRef, businessDate);
                termDepositRepository.save(contract);
                closedNormal++;
                totalInterestPaid = totalInterestPaid.add(interest);

                log.info("[FD-MATURITY] Contract {} closed normally. Payout: {} ETB (P: {} + I: {}) | GL Ref: {}",
                        contract.getContractNo(), totalPayout, principal, interest, closingGlRef);
                log.info("[FD-MATURITY] GL Entry — DEBIT GL 2060 (FD Liability): {} ETB → CREDIT GL 1010 (Savings): {} ETB",
                        totalPayout, totalPayout);
            }
        }

        log.info("[FD-MATURITY] EOD maturity sweep complete. Closed: {}, Rolled: {}, Total Interest Paid: {} ETB",
                closedNormal, rolledOver, totalInterestPaid);

        return new MaturityProcessResult(matured.size(), rolledOver, closedNormal, BigDecimal.ZERO, totalInterestPaid);
    }

    // ── 4. EARLY BREAK / PREMATURE TERMINATION ────────────────────────────────

    @Override
    @Transactional
    public EarlyBreakResult breakTermDepositEarly(String contractNo, String reason, UUID authorizedByUserId) {
        TermDepositContract contract = termDepositRepository.findByContractNo(contractNo)
                .orElseThrow(() -> new IllegalArgumentException("Term deposit contract not found: " + contractNo));

        EarlyBreakResult result = breakTermDepositEarly(contract.getContractId(), authorizedByUserId, reason);
        return new EarlyBreakResult(
                result.contractNo(),
                result.principal(),
                result.penaltyCharged(),
                result.netPayout(),
                contract.getAccountNo(),
                result.glRef(),
                "CLOSED_EARLY"
        );
    }

    @Transactional
    public EarlyBreakResult breakTermDepositEarly(UUID contractId, UUID checkerUserId, String checkerNotes) {
        TermDepositContract contract = termDepositRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Term deposit contract not found: " + contractId));

        if (contract.getStatus() != TermDepositStatus.ACTIVE) {
            throw new IllegalStateException("Contract " + contract.getContractNo() +
                    " is not ACTIVE — current status: " + contract.getStatus());
        }

        if (checkerUserId != null && checkerUserId.equals(contract.getMakerUserId())) {
            throw new SecurityException("Four-Eye violation: checker and maker cannot be the same user.");
        }

        BigDecimal principal = contract.getPrincipalAmount();
        BigDecimal accrued = contract.getAccruedInterest();
        BigDecimal penaltyRate = contract.getEarlyBreakPenaltyPct();

        BigDecimal penalty = principal.multiply(penaltyRate)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        BigDecimal netPayout = principal.add(accrued).subtract(penalty);
        if (netPayout.compareTo(BigDecimal.ZERO) < 0) {
            netPayout = principal;
            penalty = accrued;
        }

        String closingGlRef = "JE-FD-EARLY-" + contract.getContractNo();
        contract.closeEarly(penalty, netPayout, closingGlRef, LocalDate.now());
        termDepositRepository.save(contract);

        log.info("[FD-BREAK] Contract {} broken early. Principal: {} | Accrued: {} | Penalty ({}%): {} | Net Payout: {} ETB | GL Ref: {}",
                contract.getContractNo(), principal, accrued, penaltyRate, penalty, netPayout, closingGlRef);

        return new EarlyBreakResult(
                contract.getContractNo(),
                principal,
                penalty,
                netPayout,
                contract.getAccountNo(),
                closingGlRef,
                "CLOSED_EARLY"
        );
    }

    // ── 5. QUERY METHODS ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<TermDepositContract> getContract(String contractNo) {
        return termDepositRepository.findByContractNo(contractNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TermDepositContract> getContractsByAccount(String accountNo) {
        return termDepositRepository.findByAccountNo(accountNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TermDepositContract> getContractsByUser(UUID userId) {
        return termDepositRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TermDepositContract> getPendingApprovals() {
        return termDepositRepository.findPendingApprovals();
    }

    @Override
    @Transactional
    public void approveContract(String contractNo, UUID checkerUserId, String checkerNotes) {
        TermDepositContract contract = termDepositRepository.findByContractNo(contractNo)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractNo));
        contract.approve(checkerUserId, checkerNotes, "JE-FD-OPEN-" + contractNo);
        termDepositRepository.save(contract);
    }

    @Transactional(readOnly = true)
    public Optional<TermDepositContract> getContractById(UUID contractId) {
        return termDepositRepository.findById(contractId);
    }

    @Transactional(readOnly = true)
    public List<TermDepositContract> getPendingApprovalContracts() {
        return termDepositRepository.findPendingApprovals();
    }

    @Transactional(readOnly = true)
    public List<TermDepositContract> getAllActiveContracts() {
        return termDepositRepository.findByStatus(TermDepositStatus.ACTIVE);
    }
}
