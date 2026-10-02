package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.SystemBusinessDate;
import com.kab.qershi.account.domain.ports.outbound.SystemBusinessDateRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.SpringDataSystemBusinessDateRepository;
import com.kab.qershi.account.infrastructure.persistence.SystemBusinessDateEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter implementing SystemBusinessDateRepositoryPort.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class SystemBusinessDateRepositoryAdapter implements SystemBusinessDateRepositoryPort {

    private final SpringDataSystemBusinessDateRepository repository;

    public SystemBusinessDateRepositoryAdapter(SpringDataSystemBusinessDateRepository repository) {
        this.repository = repository;
    }

    @Override
    public SystemBusinessDate save(SystemBusinessDate domain) {
        SystemBusinessDateEntity entity = toEntity(domain);
        SystemBusinessDateEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SystemBusinessDate> findCurrentBusinessDate() {
        return repository.findCurrentBusinessDate().map(this::toDomain);
    }

    private SystemBusinessDate toDomain(SystemBusinessDateEntity entity) {
        if (entity == null) return null;
        return new SystemBusinessDate(
                entity.getId(),
                entity.getCurrentBusinessDate(),
                entity.getStatus(),
                entity.getIsMonthEnd(),
                entity.getLastEodCompletedAt(),
                entity.getUpdatedByUserId(),
                entity.getUpdatedAt()
        );
    }

    private SystemBusinessDateEntity toEntity(SystemBusinessDate domain) {
        if (domain == null) return null;
        return new SystemBusinessDateEntity(
                domain.getId(),
                domain.getCurrentBusinessDate(),
                domain.getStatus(),
                domain.getIsMonthEnd(),
                domain.getLastEodCompletedAt(),
                domain.getUpdatedByUserId(),
                domain.getUpdatedAt()
        );
    }
}
