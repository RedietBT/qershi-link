package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.DividendAllocation;
import com.kab.qershi.account.domain.model.DividendDistribution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Repository Port for Dividend Distribution & Allocation persistence.
 * Depends only on Domain Models — no JPA coupling.
 *
 * @author KAB Digital Solution PLC
 */
public interface DividendRepositoryPort {

    // ── Distribution CRUD ─────────────────────────────────────────────────────
    DividendDistribution saveDistribution(DividendDistribution distribution);
    Optional<DividendDistribution> findDistributionById(UUID id);
    Optional<DividendDistribution> findDistributionByFiscalYear(int fiscalYear);
    List<DividendDistribution> findAllDistributions();

    // ── Allocation CRUD ───────────────────────────────────────────────────────
    DividendAllocation saveAllocation(DividendAllocation allocation);
    List<DividendAllocation> saveAllAllocations(List<DividendAllocation> allocations);
    List<DividendAllocation> findAllocationsByDistributionId(UUID distributionId);
    void deleteAllocationsByDistributionId(UUID distributionId);
}
