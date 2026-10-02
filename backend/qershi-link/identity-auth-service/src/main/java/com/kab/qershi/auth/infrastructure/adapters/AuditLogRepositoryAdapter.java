package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.model.AuditLog;
import com.kab.qershi.auth.domain.ports.outbound.AuditLogRepositoryPort;
import com.kab.qershi.auth.infrastructure.persistence.AuditLogEntity;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataAuditLogRepository;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataUserRepository;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound persistence adapter implementing AuditLogRepositoryPort.
 * Translates between AuditLog domain models and AuditLogEntity persistence records.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final SpringDataAuditLogRepository auditLogRepository;
    private final SpringDataUserRepository userRepository;

    public AuditLogRepositoryAdapter(SpringDataAuditLogRepository auditLogRepository,
                                     SpringDataUserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AuditLog save(AuditLog domain) {
        AuditLogEntity entity = new AuditLogEntity(
                domain.getLogId(),
                domain.getUserId(),
                domain.getSaccoId(),
                domain.getAction(),
                domain.getResourceAffected(),
                domain.getStatus(),
                domain.getIpAddress(),
                domain.getDetails(),
                domain.getTimestamp()
        );
        AuditLogEntity saved = auditLogRepository.save(entity);
        return toDomain(saved, domain.getUserMsisdn());
    }

    @Override
    public List<AuditLog> findAll(int page, int size) {
        Page<AuditLogEntity> paged = auditLogRepository.findAllByOrderByTimestampDesc(
                PageRequest.of(page, Math.min(size, 200)));
        return enrichAndMap(paged.getContent());
    }

    @Override
    public List<AuditLog> findBySaccoId(UUID saccoId) {
        List<AuditLogEntity> entities = auditLogRepository.findBySaccoIdOrderByTimestampDesc(saccoId);
        return enrichAndMap(entities);
    }

    @Override
    public List<AuditLog> findByUserId(UUID userId) {
        List<AuditLogEntity> entities = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        return enrichAndMap(entities);
    }

    private List<AuditLog> enrichAndMap(List<AuditLogEntity> entities) {
        Set<UUID> userIds = entities.stream()
                .map(AuditLogEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, String> msisdnMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, UserEntity::getMsisdn));

        return entities.stream()
                .map(e -> toDomain(e, msisdnMap.get(e.getUserId())))
                .collect(Collectors.toList());
    }

    private AuditLog toDomain(AuditLogEntity entity, String msisdn) {
        AuditLog domain = new AuditLog(
                entity.getLogId(),
                entity.getUserId(),
                entity.getSaccoId(),
                entity.getAction(),
                entity.getResourceAffected(),
                entity.getStatus(),
                entity.getIpAddress(),
                entity.getDetails(),
                entity.getTimestamp()
        );
        domain.setUserMsisdn(msisdn);
        return domain;
    }
}
