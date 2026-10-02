package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.DividendAllocation;
import com.kab.qershi.account.domain.model.DividendDistribution;
import com.kab.qershi.account.domain.ports.outbound.DividendRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.DividendAllocationEntity;
import com.kab.qershi.account.infrastructure.persistence.DividendDistributionEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataDividendAllocationRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataDividendDistributionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure Adapter — bridges Dividend domain model and JPA persistence.
 * Lives in the infrastructure layer — the domain has ZERO knowledge of this class.
 *
 * @author KAB Digital Solution PLC
 */
@Component
public class DividendRepositoryAdapter implements DividendRepositoryPort {

    private final SpringDataDividendDistributionRepository distributionRepo;
    private final SpringDataDividendAllocationRepository allocationRepo;

    public DividendRepositoryAdapter(
            SpringDataDividendDistributionRepository distributionRepo,
            SpringDataDividendAllocationRepository allocationRepo) {
        this.distributionRepo = distributionRepo;
        this.allocationRepo = allocationRepo;
    }

    // ── Distribution ─────────────────────────────────────────────────────────

    @Override
    public DividendDistribution saveDistribution(DividendDistribution domain) {
        DividendDistributionEntity entity = toDistributionEntity(domain);
        return toDistributionDomain(distributionRepo.save(entity));
    }

    @Override
    public Optional<DividendDistribution> findDistributionById(UUID id) {
        return distributionRepo.findById(id).map(this::toDistributionDomain);
    }

    @Override
    public Optional<DividendDistribution> findDistributionByFiscalYear(int fiscalYear) {
        return distributionRepo.findByFiscalYear(fiscalYear).map(this::toDistributionDomain);
    }

    @Override
    public List<DividendDistribution> findAllDistributions() {
        return distributionRepo.findAll().stream()
                .map(this::toDistributionDomain)
                .collect(Collectors.toList());
    }

    // ── Allocation ────────────────────────────────────────────────────────────

    @Override
    public DividendAllocation saveAllocation(DividendAllocation domain) {
        return toAllocationDomain(allocationRepo.save(toAllocationEntity(domain)));
    }

    @Override
    public List<DividendAllocation> saveAllAllocations(List<DividendAllocation> allocations) {
        List<DividendAllocationEntity> entities = allocations.stream()
                .map(this::toAllocationEntity).collect(Collectors.toList());
        return allocationRepo.saveAll(entities).stream()
                .map(this::toAllocationDomain).collect(Collectors.toList());
    }

    @Override
    public List<DividendAllocation> findAllocationsByDistributionId(UUID distributionId) {
        return allocationRepo.findByDistributionId(distributionId).stream()
                .map(this::toAllocationDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllocationsByDistributionId(UUID distributionId) {
        allocationRepo.deleteByDistributionId(distributionId);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private DividendDistribution toDistributionDomain(DividendDistributionEntity e) {
        return new DividendDistribution(
                e.getId(), e.getFiscalYear(), e.getNetProfitPool(),
                e.getDeclaredRatePercent(), e.getTotalDividendDistributed(),
                e.getTotalTaxWithheld(), e.getQualifiedMembersCount(),
                e.getStatus(), e.getSimulatedAt(), e.getPostedAt(),
                e.getPostedBy(), e.getGlJournalRef(), e.getCreatedAt()
        );
    }

    private DividendDistributionEntity toDistributionEntity(DividendDistribution d) {
        DividendDistributionEntity e = new DividendDistributionEntity();
        e.setId(d.getId());
        e.setFiscalYear(d.getFiscalYear());
        e.setNetProfitPool(d.getNetProfitPool());
        e.setDeclaredRatePercent(d.getDeclaredRatePercent());
        e.setTotalDividendDistributed(d.getTotalDividendDistributed());
        e.setTotalTaxWithheld(d.getTotalTaxWithheld());
        e.setQualifiedMembersCount(d.getQualifiedMembersCount());
        e.setStatus(d.getStatus());
        e.setSimulatedAt(d.getSimulatedAt());
        e.setPostedAt(d.getPostedAt());
        e.setPostedBy(d.getPostedBy());
        e.setGlJournalRef(d.getGlJournalRef());
        if (d.getCreatedAt() != null) e.setCreatedAt(d.getCreatedAt());
        return e;
    }

    private DividendAllocation toAllocationDomain(DividendAllocationEntity e) {
        return new DividendAllocation(
                e.getId(), e.getDistributionId(), e.getMemberId(),
                e.getShareAccountId(), e.getDestinationAccountId(),
                e.getDestinationAccountNumber(), e.getWeightedAverageShares(),
                e.getGrossDividend(), e.getTaxWithheld(), e.getNetDividendPayout(),
                e.getStatus(), e.getFailureReason(), e.getGlJournalRef(), e.getCreatedAt()
        );
    }

    private DividendAllocationEntity toAllocationEntity(DividendAllocation a) {
        DividendAllocationEntity e = new DividendAllocationEntity();
        e.setId(a.getId());
        e.setDistributionId(a.getDistributionId());
        e.setMemberId(a.getMemberId());
        e.setShareAccountId(a.getShareAccountId());
        e.setDestinationAccountId(a.getDestinationAccountId());
        e.setDestinationAccountNumber(a.getDestinationAccountNumber());
        e.setWeightedAverageShares(a.getWeightedAverageShares());
        e.setGrossDividend(a.getGrossDividend());
        e.setTaxWithheld(a.getTaxWithheld());
        e.setNetDividendPayout(a.getNetDividendPayout());
        e.setStatus(a.getStatus());
        e.setFailureReason(a.getFailureReason());
        e.setGlJournalRef(a.getGlJournalRef());
        if (a.getCreatedAt() != null) e.setCreatedAt(a.getCreatedAt());
        return e;
    }
}
