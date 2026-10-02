package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.StandingOrder;
import com.kab.qershi.account.domain.ports.outbound.StandingOrderRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.SpringDataStandingOrderRepository;
import com.kab.qershi.account.infrastructure.persistence.StandingOrderEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter bridging StandingOrder domain model and JPA entity.
 *
 * @author KAB Digital Solution PLC
 */
@Component
public class StandingOrderRepositoryAdapter implements StandingOrderRepositoryPort {

    private final SpringDataStandingOrderRepository repository;

    public StandingOrderRepositoryAdapter(SpringDataStandingOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public StandingOrder save(StandingOrder domain) {
        return toDomain(repository.save(toEntity(domain)));
    }

    @Override
    public Optional<StandingOrder> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<StandingOrder> findByStandingOrderNo(String standingOrderNo) {
        return repository.findByStandingOrderNo(standingOrderNo).map(this::toDomain);
    }

    @Override
    public List<StandingOrder> findByMemberId(UUID memberId) {
        return repository.findByMemberId(memberId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StandingOrder> findBySourceAccountId(UUID sourceAccountId) {
        return repository.findBySourceAccountId(sourceAccountId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StandingOrder> findDueOrders(LocalDate asOfDate) {
        return repository.findDueOrders(asOfDate).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StandingOrder> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private StandingOrder toDomain(StandingOrderEntity e) {
        return new StandingOrder(
                e.getId(), e.getStandingOrderNo(),
                e.getSourceAccountId(), e.getSourceAccountNo(),
                e.getTargetAccountId(), e.getTargetAccountNo(),
                e.getMemberId(), e.getAmount(), e.getFrequency(),
                e.getDayOfMonth(), e.getDayOfWeek(),
                e.getStartDate(), e.getNextRunDate(), e.getEndDate(),
                e.getTotalExecutionsCount(), e.getFailedAttemptsCount(),
                e.getDescription(), e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private StandingOrderEntity toEntity(StandingOrder d) {
        StandingOrderEntity e = new StandingOrderEntity();
        e.setId(d.getId());
        e.setStandingOrderNo(d.getStandingOrderNo());
        e.setSourceAccountId(d.getSourceAccountId());
        e.setSourceAccountNo(d.getSourceAccountNo());
        e.setTargetAccountId(d.getTargetAccountId());
        e.setTargetAccountNo(d.getTargetAccountNo());
        e.setMemberId(d.getMemberId());
        e.setAmount(d.getAmount());
        e.setFrequency(d.getFrequency());
        e.setDayOfMonth(d.getDayOfMonth());
        e.setDayOfWeek(d.getDayOfWeek());
        e.setStartDate(d.getStartDate());
        e.setNextRunDate(d.getNextRunDate());
        e.setEndDate(d.getEndDate());
        e.setTotalExecutionsCount(d.getTotalExecutionsCount());
        e.setFailedAttemptsCount(d.getFailedAttemptsCount());
        e.setDescription(d.getDescription());
        e.setStatus(d.getStatus());
        if (d.getCreatedAt() != null) e.setCreatedAt(d.getCreatedAt());
        if (d.getUpdatedAt() != null) e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}
