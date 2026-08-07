package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.port.out.RepaymentScheduleRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.RepaymentScheduleEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataRepaymentScheduleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Repository Adapter implementing RepaymentScheduleRepositoryPort via JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class RepaymentScheduleRepositoryAdapter implements RepaymentScheduleRepositoryPort {

    private final SpringDataRepaymentScheduleRepository repository;

    public RepaymentScheduleRepositoryAdapter(SpringDataRepaymentScheduleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RepaymentSchedule> saveAll(List<RepaymentSchedule> schedules) {
        List<RepaymentScheduleEntity> entities = schedules.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        List<RepaymentScheduleEntity> saved = repository.saveAll(entities);
        return saved.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<RepaymentSchedule> findByAccountIdOrderByInstallmentNoAsc(UUID accountId) {
        return repository.findByAccountIdOrderByInstallmentNoAsc(accountId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public RepaymentSchedule save(RepaymentSchedule schedule) {
        RepaymentScheduleEntity entity = toEntity(schedule);
        RepaymentScheduleEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private RepaymentScheduleEntity toEntity(RepaymentSchedule domain) {
        if (domain == null) return null;
        RepaymentScheduleEntity entity = new RepaymentScheduleEntity();
        entity.setScheduleId(domain.getScheduleId());
        entity.setAccountId(domain.getAccountId());
        entity.setInstallmentNo(domain.getInstallmentNo());
        entity.setDueDate(domain.getDueDate());
        entity.setPrincipalDue(domain.getPrincipalDue());
        entity.setInterestDue(domain.getInterestDue());
        entity.setTotalDue(domain.getTotalDue());
        entity.setAmountPaid(domain.getAmountPaid());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private RepaymentSchedule toDomain(RepaymentScheduleEntity entity) {
        if (entity == null) return null;
        return new RepaymentSchedule(
                entity.getScheduleId(),
                entity.getAccountId(),
                entity.getInstallmentNo(),
                entity.getDueDate(),
                entity.getPrincipalDue(),
                entity.getInterestDue(),
                entity.getTotalDue(),
                entity.getAmountPaid(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
