package com.kab.qershi.auth.infrastructure.persistence;

import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Hexagonal Outbound Adapter translating SACCO registry tracking between domain rules and physical JPA models.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class SaccoPersistenceAdapter implements SaccoRepositoryPort {

    private final SpringDataSaccoRepository repository;

    public SaccoPersistenceAdapter(SpringDataSaccoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Sacco save(Sacco sacco) {
        SaccoEntity entity = new SaccoEntity();
        entity.setSaccoId(sacco.getSaccoId());
        entity.setParentUnionId(sacco.getParentUnionId());
        entity.setSaccoName(sacco.getSaccoName());
        entity.setSchemaName(sacco.getSchemaName());
        entity.setUnion(sacco.isUnion());
        entity.setMinShareRequirement(sacco.getMinShareRequirement());
        entity.setStatus(sacco.getStatus().name());
        entity.setCreatedAt(sacco.getCreatedAt());
        entity.setUpdatedAt(sacco.getUpdatedAt());

        repository.save(entity);
        return sacco;
    }

    @Override
    public Optional<Sacco> findById(UUID saccoId) {
        return repository.findById(saccoId).map(entity -> {
            Sacco sacco = new Sacco(entity.getSaccoId(), entity.getSaccoName(), entity.getSchemaName(), entity.isUnion(), entity.getMinShareRequirement());
            if (entity.getParentUnionId() != null) {
                sacco.attachToUnion(entity.getParentUnionId());
            }
            if ("ACTIVE".equals(entity.getStatus())) {
                sacco.activate();
            }
            return sacco;
        });
    }

    @Override
    public boolean existsBySaccoName(String saccoName) {
        return repository.existsBySaccoName(saccoName);
    }

    @Override
    public boolean existsBySchemaName(String schemaName) {
        return repository.existsBySchemaName(schemaName);
    }
}