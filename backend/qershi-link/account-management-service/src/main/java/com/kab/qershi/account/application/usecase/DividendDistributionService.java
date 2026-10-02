package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.domain.model.DividendAllocation;
import com.kab.qershi.account.domain.model.DividendDistribution;
import com.kab.qershi.account.domain.model.ShareAccount;
import com.kab.qershi.account.domain.ports.inbound.DividendDistributionUseCase;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.DividendRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ShareAccountRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hexagonal Application Service — AGM Annual Dividend Distribution Engine.
 *
 * Calculation Logic (Temenos/Finacle standard):
 *  grossDividend  = weightedAverageShares × nominalValue × (declaredRate / 100)
 *  taxWithheld    = grossDividend × 5% (NBE WHT on dividend income)
 *  netPayout      = grossDividend − taxWithheld
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class DividendDistributionService implements DividendDistributionUseCase {

    private static final Logger log = LoggerFactory.getLogger(DividendDistributionService.class);

    /** NBE statutory withholding tax on dividend income */
    private static final BigDecimal DIVIDEND_WHT_RATE = new BigDecimal("0.05");

    private final DividendRepositoryPort dividendRepository;
    private final ShareAccountRepositoryPort shareAccountRepository;
    private final AccountRepositoryPort accountRepository;

    public DividendDistributionService(
            DividendRepositoryPort dividendRepository,
            ShareAccountRepositoryPort shareAccountRepository,
            AccountRepositoryPort accountRepository) {
        this.dividendRepository = dividendRepository;
        this.shareAccountRepository = shareAccountRepository;
        this.accountRepository = accountRepository;
    }

    // ── 1. Simulate ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SimulationResult simulate(int fiscalYear, BigDecimal netProfitPool, BigDecimal declaredRatePercent) {
        if (netProfitPool == null || netProfitPool.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Net profit pool must be a positive amount.");
        }
        if (declaredRatePercent == null || declaredRatePercent.compareTo(BigDecimal.ZERO) <= 0
                || declaredRatePercent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Declared rate must be between 0% and 100%.");
        }

        // Idempotent — retrieve existing or create new DRAFT
        DividendDistribution distribution = dividendRepository.findDistributionByFiscalYear(fiscalYear)
                .orElseGet(() -> DividendDistribution.createDraft(fiscalYear, netProfitPool, declaredRatePercent));

        if (distribution.isPosted()) {
            throw new IllegalStateException("Dividend distribution for FY" + fiscalYear + " is already POSTED and cannot be re-simulated.");
        }

        // Update pool & rate in case they changed
        distribution.setNetProfitPool(netProfitPool);
        distribution.setDeclaredRatePercent(declaredRatePercent);

        // Clear prior simulation allocations (re-simulate)
        if (distribution.getId() != null) {
            dividendRepository.deleteAllocationsByDistributionId(distribution.getId());
        }
        distribution = dividendRepository.saveDistribution(distribution);

        // Load all active share accounts
        List<ShareAccount> shareAccounts = shareAccountRepository.findAllActive();

        BigDecimal rateDecimal = declaredRatePercent.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalTax   = BigDecimal.ZERO;
        List<DividendAllocation> allocations = new ArrayList<>();

        for (ShareAccount sa : shareAccounts) {
            if (sa.getTotalShares() <= 0) continue;

            // Weighted average shares (simplified: current holdings for annual AGM)
            BigDecimal weightedShares = BigDecimal.valueOf(sa.getTotalShares());
            BigDecimal shareValue = sa.getShareNominalValue();

            BigDecimal gross = weightedShares.multiply(shareValue).multiply(rateDecimal)
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal tax   = gross.multiply(DIVIDEND_WHT_RATE).setScale(4, RoundingMode.HALF_UP);
            BigDecimal net   = gross.subtract(tax);

            // Find the member's primary savings account as destination
            String destAccountNo = null;
            UUID destAccountId = null;
            try {
                List<Account> memberAccounts = accountRepository.findByMemberId(sa.getMemberId());
                Account primary = memberAccounts.stream()
                        .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                        .findFirst().orElse(null);
                if (primary != null) {
                    destAccountNo = primary.getAccountNo();
                    destAccountId = primary.getAccountId();
                }
            } catch (Exception ex) {
                log.warn("Could not resolve destination account for member {}: {}", sa.getMemberId(), ex.getMessage());
            }

            DividendAllocation alloc = new DividendAllocation(
                    UUID.randomUUID(),
                    distribution.getId(),
                    sa.getMemberId(),
                    sa.getId(),
                    destAccountId,
                    destAccountNo,
                    weightedShares,
                    gross,
                    tax,
                    net,
                    DividendAllocation.STATUS_PENDING,
                    null, null,
                    java.time.OffsetDateTime.now()
            );
            allocations.add(alloc);
            totalGross = totalGross.add(gross);
            totalTax   = totalTax.add(tax);
        }

        List<DividendAllocation> saved = dividendRepository.saveAllAllocations(allocations);
        BigDecimal totalNet = totalGross.subtract(totalTax);

        distribution.markSimulated(totalGross, totalTax, allocations.size());
        dividendRepository.saveDistribution(distribution);

        log.info("Dividend SIMULATION complete: FY={}, Members={}, GrossPool={} ETB, TaxWithheld={} ETB, NetPayout={} ETB",
                fiscalYear, allocations.size(), totalGross, totalTax, totalNet);

        return new SimulationResult(
                distribution.getId(), fiscalYear, netProfitPool, declaredRatePercent,
                totalGross, totalTax, totalNet, allocations.size(), saved
        );
    }

    // ── 2. Post ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PostingResult post(UUID distributionId, UUID postedByUserId) {
        DividendDistribution distribution = dividendRepository.findDistributionById(distributionId)
                .orElseThrow(() -> new IllegalArgumentException("Distribution not found: " + distributionId));

        if (!distribution.isSimulated()) {
            throw new IllegalStateException("Distribution must be in SIMULATED state to post. Current: " + distribution.getStatus());
        }

        List<DividendAllocation> allocations = dividendRepository.findAllocationsByDistributionId(distributionId);

        int success = 0;
        int failed  = 0;
        BigDecimal totalPosted = BigDecimal.ZERO;

        for (DividendAllocation alloc : allocations) {
            try {
                if (alloc.getDestinationAccountId() == null) {
                    alloc.markFailed("No destination savings account found for member.");
                    dividendRepository.saveAllocation(alloc);
                    failed++;
                    continue;
                }

                Account account = accountRepository.findById(alloc.getDestinationAccountId())
                        .orElse(null);

                if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
                    alloc.markFailed("Destination account is inactive or not found.");
                    dividendRepository.saveAllocation(alloc);
                    failed++;
                    continue;
                }

                // Credit net dividend to savings account
                account.setBookBalance(account.getBookBalance().add(alloc.getNetDividendPayout()));
                accountRepository.save(account);

                String glRef = String.format("JE-DIV-%d-%s", distribution.getFiscalYear(),
                        UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                alloc.markPosted(glRef);
                dividendRepository.saveAllocation(alloc);

                totalPosted = totalPosted.add(alloc.getNetDividendPayout());
                success++;

                log.info("Dividend POSTED: Member={}, Net={} ETB, Dest={}, GL={}",
                        alloc.getMemberId(), alloc.getNetDividendPayout(),
                        alloc.getDestinationAccountNumber(), glRef);

            } catch (Exception ex) {
                log.error("Failed to post dividend for member {}: {}", alloc.getMemberId(), ex.getMessage());
                alloc.markFailed(ex.getMessage());
                dividendRepository.saveAllocation(alloc);
                failed++;
            }
        }

        // Post the master GL journal entry
        String masterGlRef = String.format("JE-DIV-MASTER-%d-%s", distribution.getFiscalYear(),
                LocalDate.now().toString().replace("-", ""));
        distribution.markPosted(postedByUserId, masterGlRef);
        dividendRepository.saveDistribution(distribution);

        log.info("Dividend BATCH POSTING complete: FY={}, Success={}, Failed={}, TotalPosted={} ETB",
                distribution.getFiscalYear(), success, failed, totalPosted);

        return new PostingResult(
                distributionId, distribution.getFiscalYear(), masterGlRef,
                success, failed, totalPosted
        );
    }

    // ── 3. Queries ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DividendDistribution> getAllDistributions() {
        return dividendRepository.findAllDistributions();
    }

    @Override
    @Transactional(readOnly = true)
    public DividendDistribution getByFiscalYear(int fiscalYear) {
        return dividendRepository.findDistributionByFiscalYear(fiscalYear)
                .orElseThrow(() -> new IllegalArgumentException("No dividend distribution found for FY" + fiscalYear));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DividendAllocation> getAllocations(UUID distributionId) {
        return dividendRepository.findAllocationsByDistributionId(distributionId);
    }
}
