package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.TermDepositContract;
import com.kab.qershi.account.domain.model.TermDepositStatus;
import com.kab.qershi.account.domain.ports.outbound.TermDepositRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.SpringDataTermDepositRepository;
import com.kab.qershi.account.infrastructure.persistence.TermDepositContractEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter bridging TermDepositContract domain model and JPA entity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TermDepositRepositoryAdapter implements TermDepositRepositoryPort {

    private final SpringDataTermDepositRepository repository;

    public TermDepositRepositoryAdapter(SpringDataTermDepositRepository repository) {
        this.repository = repository;
    }

    @Override
    public TermDepositContract save(TermDepositContract domain) {
        TermDepositContractEntity entity = toEntity(domain);
        TermDepositContractEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TermDepositContract> findById(UUID contractId) {
        return repository.findById(contractId).map(this::toDomain);
    }

    @Override
    public Optional<TermDepositContract> findByContractNo(String contractNo) {
        return repository.findByContractNo(contractNo).map(this::toDomain);
    }

    @Override
    public List<TermDepositContract> findByAccountNo(String accountNo) {
        return repository.findByAccountNo(accountNo).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TermDepositContract> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TermDepositContract> findByStatus(TermDepositStatus status) {
        TermDepositContractEntity.TermDepositStatus entityStatus = toEntityStatus(status);
        return repository.findByStatus(entityStatus).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TermDepositContract> findMaturedContracts(LocalDate businessDate) {
        return repository.findMaturedContracts(businessDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TermDepositContract> findActiveContractsForAccrual(LocalDate businessDate) {
        return repository.findByStatusAndMaturityDateAfter(TermDepositContractEntity.TermDepositStatus.ACTIVE, businessDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TermDepositContract> findPendingApprovals() {
        return repository.findByStatusOrderByCreatedAtAsc(TermDepositContractEntity.TermDepositStatus.PENDING_APPROVAL).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countActiveByAccountNo(String accountNo) {
        return repository.countByAccountNoAndStatus(accountNo, TermDepositContractEntity.TermDepositStatus.ACTIVE);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private TermDepositContract toDomain(TermDepositContractEntity entity) {
        if (entity == null) return null;
        return new TermDepositContract(
                entity.getContractId(),
                entity.getContractNo(),
                entity.getAccountNo(),
                entity.getUserId(),
                entity.getSaccoCode(),
                entity.getBranchCode(),
                entity.getPrincipalAmount(),
                entity.getTenorMonths(),
                entity.getAgreedInterestRatePa(),
                entity.getEarlyBreakPenaltyPct(),
                entity.getStartDate(),
                entity.getMaturityDate(),
                entity.getClosedDate(),
                entity.getAccruedInterest(),
                entity.getCapitalizedInterest(),
                toDomainStatus(entity.getStatus()),
                entity.getAutoRollover(),
                entity.getRolloverTenorMonths(),
                entity.getPenaltyAmount(),
                entity.getNetPayoutAmount(),
                entity.getGlDebitAccount(),
                entity.getGlCreditAccount(),
                entity.getOpeningGlRef(),
                entity.getClosingGlRef(),
                entity.getMakerUserId(),
                entity.getMakerNotes(),
                entity.getCheckerUserId(),
                entity.getCheckerNotes(),
                entity.getApprovedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TermDepositContractEntity toEntity(TermDepositContract domain) {
        if (domain == null) return null;
        TermDepositContractEntity entity = new TermDepositContractEntity();
        entity.setContractId(domain.getContractId());
        entity.setContractNo(domain.getContractNo());
        entity.setAccountNo(domain.getAccountNo());
        entity.setUserId(domain.getUserId());
        entity.setSaccoCode(domain.getSaccoCode());
        entity.setBranchCode(domain.getBranchCode());
        entity.setPrincipalAmount(domain.getPrincipalAmount());
        entity.setTenorMonths(domain.getTenorMonths());
        entity.setAgreedInterestRatePa(domain.getAgreedInterestRatePa());
        entity.setEarlyBreakPenaltyPct(domain.getEarlyBreakPenaltyPct());
        entity.setStartDate(domain.getStartDate());
        entity.setMaturityDate(domain.getMaturityDate());
        entity.setClosedDate(domain.getClosedDate());
        entity.setAccruedInterest(domain.getAccruedInterest());
        entity.setCapitalizedInterest(domain.getCapitalizedInterest());
        entity.setStatus(toEntityStatus(domain.getStatus()));
        entity.setAutoRollover(domain.getAutoRollover());
        entity.setRolloverTenorMonths(domain.getRolloverTenorMonths());
        entity.setPenaltyAmount(domain.getPenaltyAmount());
        entity.setNetPayoutAmount(domain.getNetPayoutAmount());
        entity.setGlDebitAccount(domain.getGlDebitAccount());
        entity.setGlCreditAccount(domain.getGlCreditAccount());
        entity.setOpeningGlRef(domain.getOpeningGlRef());
        entity.setClosingGlRef(domain.getClosingGlRef());
        entity.setMakerUserId(domain.getMakerUserId());
        entity.setMakerNotes(domain.getMakerNotes());
        entity.setCheckerUserId(domain.getCheckerUserId());
        entity.setCheckerNotes(domain.getCheckerNotes());
        entity.setApprovedAt(domain.getApprovedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private TermDepositStatus toDomainStatus(TermDepositContractEntity.TermDepositStatus status) {
        if (status == null) return null;
        return TermDepositStatus.valueOf(status.name());
    }

    private TermDepositContractEntity.TermDepositStatus toEntityStatus(TermDepositStatus status) {
        if (status == null) return null;
        return TermDepositContractEntity.TermDepositStatus.valueOf(status.name());
    }
}
