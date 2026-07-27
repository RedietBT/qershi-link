package com.kab.qershi.profile.infrastructure.adapters;

import com.kab.qershi.profile.domain.model.NextOfKin;
import com.kab.qershi.profile.domain.ports.outbound.NextOfKinRepositoryPort;
import com.kab.qershi.profile.infrastructure.persistence.NextOfKinEntity;
import com.kab.qershi.profile.infrastructure.persistence.SpringDataNextOfKinRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Persistence Adapter implementing NextOfKinRepositoryPort.
 * Translates between pure NextOfKin Domain Models and JPA Entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class NextOfKinRepositoryAdapter implements NextOfKinRepositoryPort {

    private final SpringDataNextOfKinRepository nextOfKinRepository;

    public NextOfKinRepositoryAdapter(SpringDataNextOfKinRepository nextOfKinRepository) {
        this.nextOfKinRepository = nextOfKinRepository;
    }

    @Override
    public NextOfKin saveNextOfKin(NextOfKin domain) {
        NextOfKinEntity entity = toEntity(domain);
        NextOfKinEntity saved = nextOfKinRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<NextOfKin> findById(UUID kinId) {
        return nextOfKinRepository.findById(kinId).map(this::toDomain);
    }

    @Override
    public List<NextOfKin> findByUserId(UUID userId) {
        return nextOfKinRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID kinId) {
        nextOfKinRepository.deleteById(kinId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        nextOfKinRepository.deleteByUserId(userId);
    }

    // --- Domain ⇄ Entity Mappers ---

    private NextOfKinEntity toEntity(NextOfKin domain) {
        if (domain == null) return null;
        return new NextOfKinEntity(
                domain.getKinId(),
                domain.getUserId(),
                domain.getFullName(),
                domain.getRelationship(),
                domain.getPrimaryPhone(),
                domain.getIdNumber(),
                domain.getPhysicalAddress(),
                domain.getAllocationPercentage(),
                domain.getCreatedAt()
        );
    }

    private NextOfKin toDomain(NextOfKinEntity entity) {
        if (entity == null) return null;
        return new NextOfKin(
                entity.getKinId(),
                entity.getUserId(),
                entity.getFullName(),
                entity.getRelationship(),
                entity.getPrimaryPhone(),
                entity.getIdNumber(),
                entity.getPhysicalAddress(),
                entity.getAllocationPercentage()
        );
    }
}
