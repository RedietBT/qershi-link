package com.kab.qershi.transaction.infrastructure.adapters;

import com.kab.qershi.transaction.domain.model.TellerTill;
import com.kab.qershi.transaction.domain.model.TillCashReconciliation;
import com.kab.qershi.transaction.domain.model.TillClosingLog;
import com.kab.qershi.transaction.domain.model.TillDenomination;
import com.kab.qershi.transaction.domain.model.TillStatus;
import com.kab.qershi.transaction.domain.ports.outbound.TellerTillRepositoryPort;
import com.kab.qershi.transaction.infrastructure.persistence.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository adapter for Teller Drawer (Till) aggregate and related entities.
 * Implements TellerTillRepositoryPort, bridging pure domain models with Spring Data JPA entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TellerTillRepositoryAdapter implements TellerTillRepositoryPort {

    private final SpringDataTellerTillRepository tillRepository;
    private final SpringDataTillCashReconciliationRepository reconciliationRepository;
    private final SpringDataTillDenominationRepository denominationRepository;
    private final SpringDataTillClosingLogRepository closingLogRepository;

    public TellerTillRepositoryAdapter(SpringDataTellerTillRepository tillRepository,
                                       SpringDataTillCashReconciliationRepository reconciliationRepository,
                                       SpringDataTillDenominationRepository denominationRepository,
                                       SpringDataTillClosingLogRepository closingLogRepository) {
        this.tillRepository = tillRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.denominationRepository = denominationRepository;
        this.closingLogRepository = closingLogRepository;
    }

    @Override
    public TellerTill saveTill(TellerTill till) {
        if (till == null) return null;
        TellerTillEntity entity = toEntity(till);
        TellerTillEntity saved = tillRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TellerTill> findTillByTellerUserId(UUID tellerUserId) {
        return tillRepository.findByTellerUserId(tellerUserId).map(this::toDomain);
    }

    @Override
    public Optional<TellerTill> findTillByTellerUserIdAndStatus(UUID tellerUserId, TillStatus status) {
        return tillRepository.findByTellerUserIdAndStatus(tellerUserId, status).map(this::toDomain);
    }

    @Override
    public List<TellerTill> findTillsByBranchId(UUID branchId) {
        return tillRepository.findByBranchId(branchId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public TillCashReconciliation saveReconciliation(TillCashReconciliation reconciliation) {
        if (reconciliation == null) return null;
        TillCashReconciliationEntity entity = toEntity(reconciliation);
        TillCashReconciliationEntity saved = reconciliationRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TillCashReconciliation> findReconciliationById(UUID reconciliationId) {
        return reconciliationRepository.findById(reconciliationId).map(this::toDomain);
    }

    @Override
    public List<TillCashReconciliation> findReconciliationsByTillId(UUID tillId) {
        return reconciliationRepository.findByTillIdOrderByCreatedAtDesc(tillId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<TillCashReconciliation> findReconciliationsByTellerUserId(UUID tellerUserId) {
        return reconciliationRepository.findByTellerUserIdOrderByCreatedAtDesc(tellerUserId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveDenominations(List<TillDenomination> denominations) {
        if (denominations == null || denominations.isEmpty()) return;
        List<TillDenominationEntity> entities = denominations.stream()
                .map(this::toEntity)
                .toList();
        denominationRepository.saveAll(entities);
    }

    @Override
    public List<TillDenomination> findDenominationsByReconciliationId(UUID reconciliationId) {
        return denominationRepository.findByReconciliationIdOrderByDenominationValueDesc(reconciliationId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public TillClosingLog saveClosingLog(TillClosingLog closingLog) {
        if (closingLog == null) return null;
        TillClosingLogEntity entity = toEntity(closingLog);
        TillClosingLogEntity saved = closingLogRepository.save(entity);
        return toDomain(saved);
    }

    // Mapping helpers: Till
    private TellerTill toDomain(TellerTillEntity entity) {
        if (entity == null) return null;
        return new TellerTill(
                entity.getTillId(),
                entity.getBranchId(),
                entity.getBranchCode(),
                entity.getTellerUserId(),
                entity.getTillName(),
                entity.getTillGlCode(),
                entity.getOpeningCash(),
                entity.getCurrentCash(),
                entity.getMaxCashLimit(),
                entity.getStatus(),
                entity.getOpenedAt(),
                entity.getClosedAt(),
                entity.getCreatedAt()
        );
    }

    private TellerTillEntity toEntity(TellerTill domain) {
        if (domain == null) return null;
        TellerTillEntity entity = new TellerTillEntity();
        entity.setTillId(domain.getTillId());
        entity.setBranchId(domain.getBranchId());
        entity.setBranchCode(domain.getBranchCode());
        entity.setTellerUserId(domain.getTellerUserId());
        entity.setTillName(domain.getTillName());
        entity.setTillGlCode(domain.getTillGlCode());
        entity.setOpeningCash(domain.getOpeningCash());
        entity.setCurrentCash(domain.getCurrentCash());
        entity.setMaxCashLimit(domain.getMaxCashLimit());
        entity.setStatus(domain.getStatus());
        entity.setOpenedAt(domain.getOpenedAt());
        entity.setClosedAt(domain.getClosedAt());
        return entity;
    }

    // Mapping helpers: TillCashReconciliation
    private TillCashReconciliation toDomain(TillCashReconciliationEntity entity) {
        if (entity == null) return null;
        return new TillCashReconciliation(
                entity.getReconciliationId(),
                entity.getTillId(),
                entity.getTellerUserId(),
                entity.getElectronicCashBalance(),
                entity.getPhysicalCashCounted(),
                entity.getCashVariance(),
                entity.getNotes200Count(),
                entity.getNotes100Count(),
                entity.getNotes50Count(),
                entity.getNotes10Count(),
                entity.getNotes5Count(),
                entity.getCoinsAmount(),
                entity.getVarianceType(),
                entity.getVarianceAmount(),
                entity.getVarianceGlCode(),
                entity.getJournalEntryId(),
                entity.getStatus(),
                entity.getSupervisorApprovedBy(),
                entity.getSupervisorApprovedAt(),
                entity.getSupervisorNotes(),
                entity.getReconciliationNotes(),
                entity.getCreatedAt()
        );
    }

    private TillCashReconciliationEntity toEntity(TillCashReconciliation domain) {
        if (domain == null) return null;
        TillCashReconciliationEntity entity = new TillCashReconciliationEntity(
                domain.getTillId(),
                domain.getTellerUserId(),
                domain.getElectronicCashBalance(),
                domain.getPhysicalCashCounted(),
                domain.getCashVariance(),
                domain.getNotes200Count(),
                domain.getNotes100Count(),
                domain.getNotes50Count(),
                domain.getNotes10Count(),
                domain.getNotes5Count(),
                domain.getCoinsAmount(),
                domain.getVarianceType(),
                domain.getVarianceAmount(),
                domain.getVarianceGlCode(),
                domain.getJournalEntryId(),
                domain.getStatus(),
                domain.getReconciliationNotes()
        );
        entity.setReconciliationId(domain.getReconciliationId());
        entity.setSupervisorApprovedBy(domain.getSupervisorApprovedBy());
        entity.setSupervisorApprovedAt(domain.getSupervisorApprovedAt());
        entity.setSupervisorNotes(domain.getSupervisorNotes());
        return entity;
    }

    // Mapping helpers: TillDenomination
    private TillDenomination toDomain(TillDenominationEntity entity) {
        if (entity == null) return null;
        return new TillDenomination(
                entity.getDenominationId(),
                entity.getReconciliationId(),
                entity.getDenominationValue(),
                entity.getQuantity(),
                entity.getTotalAmount(),
                entity.getCreatedAt()
        );
    }

    private TillDenominationEntity toEntity(TillDenomination domain) {
        if (domain == null) return null;
        TillDenominationEntity entity = new TillDenominationEntity(
                domain.getReconciliationId(),
                domain.getDenominationValue(),
                domain.getQuantity(),
                domain.getTotalAmount()
        );
        entity.setDenominationId(domain.getDenominationId());
        return entity;
    }

    // Mapping helpers: TillClosingLog
    private TillClosingLog toDomain(TillClosingLogEntity entity) {
        if (entity == null) return null;
        return new TillClosingLog(
                entity.getLogId(),
                entity.getReconciliationId(),
                entity.getTillId(),
                entity.getTellerUserId(),
                entity.getClosingMode(),
                entity.getElectronicBalance(),
                entity.getPhysicalTotal(),
                entity.getVariance(),
                entity.getStatus(),
                entity.getJournalEntryId(),
                entity.getCreatedAt()
        );
    }

    private TillClosingLogEntity toEntity(TillClosingLog domain) {
        if (domain == null) return null;
        TillClosingLogEntity entity = new TillClosingLogEntity(
                domain.getReconciliationId(),
                domain.getTillId(),
                domain.getTellerUserId(),
                domain.getClosingMode(),
                domain.getElectronicBalance(),
                domain.getPhysicalTotal(),
                domain.getVariance(),
                domain.getStatus(),
                domain.getJournalEntryId()
        );
        entity.setLogId(domain.getLogId());
        return entity;
    }
}
