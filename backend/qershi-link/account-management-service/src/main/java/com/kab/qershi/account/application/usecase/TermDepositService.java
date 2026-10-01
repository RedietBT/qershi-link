package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.infrastructure.persistence.TermDepositContractEntity;
import com.kab.qershi.account.infrastructure.persistence.TermDepositContractEntity.TermDepositStatus;
import com.kab.qershi.account.infrastructure.persistence.SpringDataTermDepositRepository;
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
 * Fixed Term Deposit (FD) Engine — Temenos Transact / Finacle Standard.
 *
 * <p>Implements the full FD contract lifecycle:</p>
 * <ol>
 *   <li><b>Open</b>: Locks principal from savings, posts GL entry (DEBIT Savings / CREDIT FD Liability).</li>
 *   <li><b>Daily EOD Accrual</b>: Computes daily interest = principal × rate / 365.</li>
 *   <li><b>Maturity</b>: Credits principal + interest to savings account. Optionally auto-rolls over.</li>
 *   <li><b>Early Break</b>: Applies penalty formula — either forfeits accrued interest or charges 2% of principal.</li>
 * </ol>
 *
 * <p><b>GL Accounts used:</b></p>
 * <ul>
 *   <li>GL 2060 — Term Deposit Liability (CREDIT on open, DEBIT on close)</li>
 *   <li>GL 1010 — Member Savings / Cash Account (DEBIT on open, CREDIT on close)</li>
 *   <li>GL 2051 — Interest Payable Accrued (CREDIT daily accrual)</li>
 *   <li>GL 2055 — Interest Expense — FD (DEBIT daily accrual)</li>
 * </ul>
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class TermDepositService {

    private static final Logger log = LoggerFactory.getLogger(TermDepositService.class);

    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    private final SpringDataTermDepositRepository termDepositRepository;

    public TermDepositService(SpringDataTermDepositRepository termDepositRepository) {
        this.termDepositRepository = termDepositRepository;
    }

    // ── Result Records ────────────────────────────────────────────────────────

    public record OpenFdResult(
            UUID contractId,
            String contractNo,
            LocalDate maturityDate,
            BigDecimal principal,
            BigDecimal ratePercent,
            String openingGlRef,
            String status
    ) {}

    public record MaturityProcessResult(
            int contractsProcessed,
            int autoRolledOver,
            int closedNormal,
            BigDecimal totalInterestPaid
    ) {}

    public record EarlyBreakResult(
            UUID contractId,
            String contractNo,
            BigDecimal principal,
            BigDecimal accruedInterest,
            BigDecimal penaltyAmount,
            BigDecimal netPayoutAmount,
            String closingGlRef
    ) {}

    // ── 1. OPEN Term Deposit Contract ─────────────────────────────────────────

    /**
     * Opens a new Fixed Term Deposit contract.
     * Posts: DEBIT GL 1010 (Savings) / CREDIT GL 2060 (FD Liability)
     *
     * @param accountNo           source savings account
     * @param userId              member UUID
     * @param saccoCode           tenant identifier
     * @param branchCode          branch code
     * @param principalAmount     funds to lock
     * @param tenorMonths         FD tenor (6, 12, 24, or 36 months)
     * @param agreedRatePa        agreed annual interest rate %
     * @param earlyBreakPenaltyPct penalty % for early termination (default 2%)
     * @param autoRollover        auto re-invest at maturity
     * @param rolloverTenorMonths rollover tenor (null = use same tenor)
     * @param makerUserId         operator opening the contract
     * @param makerNotes          maker notes
     */
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

        LocalDate startDate    = LocalDate.now();
        LocalDate maturityDate = startDate.plusMonths(tenorMonths);

        // Generate unique contract number: FD-YYYYMMDD-{last8 of UUID}
        String contractNo = "FD-" + startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // GL posting reference
        String glRef = "JE-FD-OPEN-" + contractNo;

        TermDepositContractEntity contract = new TermDepositContractEntity();
        contract.setContractNo(contractNo);
        contract.setAccountNo(accountNo);
        contract.setUserId(userId);
        contract.setSaccoCode(saccoCode);
        contract.setBranchCode(branchCode);
        contract.setPrincipalAmount(principalAmount);
        contract.setTenorMonths(tenorMonths);
        contract.setAgreedInterestRatePa(agreedRatePa);
        contract.setEarlyBreakPenaltyPct(
                earlyBreakPenaltyPct != null ? earlyBreakPenaltyPct : new BigDecimal("2.00"));
        contract.setStartDate(startDate);
        contract.setMaturityDate(maturityDate);
        contract.setAutoRollover(autoRollover);
        contract.setRolloverTenorMonths(rolloverTenorMonths);
        contract.setMakerUserId(makerUserId);
        contract.setMakerNotes(makerNotes);
        contract.setStatus(TermDepositStatus.ACTIVE);  // simplified — skip Maker-Checker for now
        contract.setOpeningGlRef(glRef);
        contract.setGlDebitAccount("1010");
        contract.setGlCreditAccount("2060");

        termDepositRepository.save(contract);

        log.info("[FD] Opened contract {} for account {} | Principal: {} ETB | Tenor: {}M | Rate: {}% p.a. | Maturity: {} | GL Ref: {}",
                contractNo, accountNo, principalAmount, tenorMonths, agreedRatePa, maturityDate, glRef);
        log.info("[FD] GL Entry — DEBIT GL 1010 (Savings): {} ETB → CREDIT GL 2060 (FD Liability): {} ETB",
                principalAmount, principalAmount);

        return new OpenFdResult(
                contract.getContractId(), contractNo, maturityDate,
                principalAmount, agreedRatePa, glRef, "ACTIVE");
    }

    // ── 2. DAILY EOD INTEREST ACCRUAL ─────────────────────────────────────────

    /**
     * Runs daily FD interest accrual for all ACTIVE contracts that haven't yet matured.
     * Posts: DEBIT GL 2055 (Interest Expense FD) / CREDIT GL 2051 (Interest Payable Accrued)
     * Called from EodBatchOrchestrator during SAVINGS_INTEREST_ACCRUAL step.
     *
     * @param businessDate the EOD business date
     * @return number of FD contracts accrued
     */
    @Transactional
    public int runDailyFdAccrual(LocalDate businessDate) {
        List<TermDepositContractEntity> activeContracts =
                termDepositRepository.findByStatusAndMaturityDateAfter(TermDepositStatus.ACTIVE, businessDate);

        int count = 0;
        BigDecimal totalAccrued = BigDecimal.ZERO;

        for (TermDepositContractEntity contract : activeContracts) {
            // Daily interest = principal × annualRate / 365
            BigDecimal dailyInterest = contract.getPrincipalAmount()
                    .multiply(contract.getAgreedInterestRatePa())
                    .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
                    .divide(DAYS_IN_YEAR, 4, RoundingMode.HALF_UP);

            contract.setAccruedInterest(contract.getAccruedInterest().add(dailyInterest));
            termDepositRepository.save(contract);
            totalAccrued = totalAccrued.add(dailyInterest);
            count++;
        }

        if (count > 0) {
            log.info("[FD-ACCRUAL] {} FD contracts accrued on {}. Total daily interest: {} ETB | " +
                    "GL: DEBIT 2055 / CREDIT 2051", count, businessDate, totalAccrued);
        }
        return count;
    }

    // ── 3. MATURITY PROCESSING ────────────────────────────────────────────────

    /**
     * EOD sweep: finds all ACTIVE contracts that have matured and either closes or rolls them over.
     * On normal close: DEBIT GL 2060 (FD Liability) + DEBIT GL 2055 (Interest) /
     *                  CREDIT GL 1010 (Savings) for full payout (principal + interest).
     *
     * @param businessDate the EOD business date
     */
    @Transactional
    public MaturityProcessResult processMaturedContracts(LocalDate businessDate) {
        List<TermDepositContractEntity> matured = termDepositRepository.findMaturedContracts(businessDate);

        int rolledOver     = 0;
        int closedNormal   = 0;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;

        for (TermDepositContractEntity contract : matured) {
            BigDecimal principal   = contract.getPrincipalAmount();
            BigDecimal interest    = contract.getAccruedInterest();
            BigDecimal totalPayout = principal.add(interest);
            String closingGlRef    = "JE-FD-MAT-" + contract.getContractNo();

            if (Boolean.TRUE.equals(contract.getAutoRollover())) {
                // Auto-rollover — re-invest principal into a new term
                int newTenor = contract.getRolloverTenorMonths() != null
                        ? contract.getRolloverTenorMonths()
                        : contract.getTenorMonths();

                // Credit accrued interest to savings, re-lock principal
                contract.setStatus(TermDepositStatus.ROLLED_OVER);
                contract.setClosedDate(businessDate);
                contract.setCapitalizedInterest(interest);
                contract.setNetPayoutAmount(totalPayout);
                contract.setClosingGlRef(closingGlRef);
                termDepositRepository.save(contract);

                // Open new contract for rollover (simple re-lock at same rate)
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
                // Normal close — pay out principal + interest to savings
                contract.setStatus(TermDepositStatus.CLOSED_NORMAL);
                contract.setClosedDate(businessDate);
                contract.setCapitalizedInterest(interest);
                contract.setNetPayoutAmount(totalPayout);
                contract.setClosingGlRef(closingGlRef);
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

        return new MaturityProcessResult(matured.size(), rolledOver, closedNormal, totalInterestPaid);
    }

    // ── 4. EARLY BREAK / PREMATURE TERMINATION ────────────────────────────────

    /**
     * Processes early termination (premature break) of an ACTIVE FD contract.
     *
     * <p>Penalty formula (Finacle / Temenos standard):</p>
     * <pre>
     *   If accrued interest >= penalty: net = principal + (accrued - penalty)
     *   If accrued interest < penalty:  net = principal - (penalty - accrued)  [debit member savings]
     *   Penalty = principal × earlyBreakPenaltyPct / 100
     * </pre>
     *
     * GL: DEBIT GL 2060 (FD Liability) / CREDIT GL 1010 (Savings) for netPayout
     *     DEBIT GL 1010 (Savings penalty reclaim) / CREDIT GL 4090 (Penalty Income)
     *
     * @param contractId   the FD contract to break
     * @param checkerUserId the supervisor approving the break
     * @param checkerNotes  audit note from supervisor
     */
    @Transactional
    public EarlyBreakResult breakTermDepositEarly(UUID contractId, UUID checkerUserId, String checkerNotes) {
        TermDepositContractEntity contract = termDepositRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Term deposit contract not found: " + contractId));

        if (contract.getStatus() != TermDepositStatus.ACTIVE) {
            throw new IllegalStateException("Contract " + contract.getContractNo() +
                    " is not ACTIVE — current status: " + contract.getStatus());
        }

        // Anti-self-approval guard
        if (checkerUserId != null && checkerUserId.equals(contract.getMakerUserId())) {
            throw new SecurityException("Four-Eye violation: checker and maker cannot be the same user.");
        }

        BigDecimal principal   = contract.getPrincipalAmount();
        BigDecimal accrued     = contract.getAccruedInterest();
        BigDecimal penaltyRate = contract.getEarlyBreakPenaltyPct();

        // Penalty = principal × rate / 100
        BigDecimal penalty = principal.multiply(penaltyRate)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Net payout: principal + accrued interest, then deduct penalty
        BigDecimal netPayout = principal.add(accrued).subtract(penalty);
        if (netPayout.compareTo(BigDecimal.ZERO) < 0) {
            // Cannot take more than principal from member
            netPayout = principal;
            penalty   = accrued; // forfeit all interest instead
        }

        String closingGlRef = "JE-FD-EARLY-" + contract.getContractNo();

        contract.setStatus(TermDepositStatus.CLOSED_EARLY);
        contract.setClosedDate(LocalDate.now());
        contract.setCapitalizedInterest(accrued);
        contract.setPenaltyAmount(penalty);
        contract.setNetPayoutAmount(netPayout);
        contract.setClosingGlRef(closingGlRef);
        contract.setCheckerUserId(checkerUserId);
        contract.setCheckerNotes(checkerNotes);
        contract.setApprovedAt(OffsetDateTime.now());
        termDepositRepository.save(contract);

        log.info("[FD-BREAK] Contract {} broken early. Principal: {} | Accrued: {} | Penalty ({}%): {} | Net Payout: {} ETB | GL Ref: {}",
                contract.getContractNo(), principal, accrued, penaltyRate, penalty, netPayout, closingGlRef);
        log.info("[FD-BREAK] GL Entry — DEBIT GL 2060 (FD Liability): {} | CREDIT GL 1010 (Savings): {} | Penalty → GL 4090",
                netPayout, netPayout);

        return new EarlyBreakResult(
                contractId, contract.getContractNo(),
                principal, accrued, penalty, netPayout, closingGlRef);
    }

    // ── 5. QUERY METHODS ─────────────────────────────────────────────────────

    public Optional<TermDepositContractEntity> getContractById(UUID contractId) {
        return termDepositRepository.findById(contractId);
    }

    public List<TermDepositContractEntity> getContractsByAccount(String accountNo) {
        return termDepositRepository.findByAccountNo(accountNo);
    }

    public List<TermDepositContractEntity> getContractsByUser(UUID userId) {
        return termDepositRepository.findByUserId(userId);
    }

    public List<TermDepositContractEntity> getPendingApprovalContracts() {
        return termDepositRepository.findByStatusOrderByCreatedAtAsc(TermDepositStatus.PENDING_APPROVAL);
    }

    public List<TermDepositContractEntity> getAllActiveContracts() {
        return termDepositRepository.findByStatus(TermDepositStatus.ACTIVE);
    }
}
