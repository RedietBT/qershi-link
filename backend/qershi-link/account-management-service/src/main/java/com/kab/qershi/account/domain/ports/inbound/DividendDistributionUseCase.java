package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.DividendAllocation;
import com.kab.qershi.account.domain.model.DividendDistribution;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound Use Case Port — AGM Annual Dividend Distribution Engine.
 *
 * Flow:
 *  1. simulate()  → calculate allocations per member, save as SIMULATED
 *  2. post()      → batch-credit member savings accounts, post GL, mark POSTED
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface DividendDistributionUseCase {

    record SimulationResult(
            UUID distributionId,
            int fiscalYear,
            BigDecimal netProfitPool,
            BigDecimal declaredRatePercent,
            BigDecimal totalGrossDividend,
            BigDecimal totalTaxWithheld,
            BigDecimal totalNetPayout,
            int qualifiedMembersCount,
            List<DividendAllocation> allocations
    ) {}

    record PostingResult(
            UUID distributionId,
            int fiscalYear,
            String glJournalRef,
            int successfulPostings,
            int failedPostings,
            BigDecimal totalNetPosted
    ) {}

    /**
     * Simulate dividend allocation without posting any GL entries or crediting accounts.
     * Idempotent — calling twice for the same fiscal year re-runs the simulation.
     */
    SimulationResult simulate(int fiscalYear, BigDecimal netProfitPool, BigDecimal declaredRatePercent);

    /**
     * Batch-post previously simulated distribution:
     * credits net dividend to each member's savings account and posts GL journal.
     */
    PostingResult post(UUID distributionId, UUID postedByUserId);

    /** All distribution headers across all fiscal years */
    List<DividendDistribution> getAllDistributions();

    /** Get distribution header for a specific fiscal year */
    DividendDistribution getByFiscalYear(int fiscalYear);

    /** Get allocations for a specific distribution run */
    List<DividendAllocation> getAllocations(UUID distributionId);
}
