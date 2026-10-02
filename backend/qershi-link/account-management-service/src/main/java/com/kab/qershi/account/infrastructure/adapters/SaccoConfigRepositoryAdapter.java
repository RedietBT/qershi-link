package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.SaccoConfig;
import com.kab.qershi.account.domain.ports.outbound.SaccoConfigRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.SaccoConfigEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataSaccoConfigRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter bridging SaccoConfig domain model and SaccoConfigEntity JPA entity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class SaccoConfigRepositoryAdapter implements SaccoConfigRepositoryPort {

    private final SpringDataSaccoConfigRepository repository;

    public SaccoConfigRepositoryAdapter(SpringDataSaccoConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public SaccoConfig save(SaccoConfig domain) {
        SaccoConfigEntity entity = toEntity(domain);
        SaccoConfigEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SaccoConfig> findFirst() {
        return repository.findFirstByOrderByCreatedAtAsc().map(this::toDomain);
    }

    private SaccoConfig toDomain(SaccoConfigEntity entity) {
        if (entity == null) return null;
        return new SaccoConfig(
                entity.getConfigId(),
                entity.getSaccoCode(),
                entity.getSaccoName(),
                entity.getBranchCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private SaccoConfigEntity toEntity(SaccoConfig domain) {
        if (domain == null) return null;
        SaccoConfigEntity entity = new SaccoConfigEntity(
                domain.getId(),
                domain.getSaccoCode(),
                domain.getSaccoName(),
                domain.getBranchCode()
        );
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
