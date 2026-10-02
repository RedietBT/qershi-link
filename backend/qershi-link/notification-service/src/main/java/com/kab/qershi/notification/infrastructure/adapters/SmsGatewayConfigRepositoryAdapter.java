package com.kab.qershi.notification.infrastructure.adapters;

import com.kab.qershi.notification.domain.model.SmsGatewayConfig;
import com.kab.qershi.notification.domain.ports.outbound.SmsGatewayConfigRepositoryPort;
import com.kab.qershi.notification.infrastructure.persistence.SmsGatewayConfigEntity;
import com.kab.qershi.notification.infrastructure.persistence.SpringDataSmsGatewayConfigRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * Persistence adapter implementing SmsGatewayConfigRepositoryPort.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class SmsGatewayConfigRepositoryAdapter implements SmsGatewayConfigRepositoryPort {

    private final SpringDataSmsGatewayConfigRepository repository;

    public SmsGatewayConfigRepositoryAdapter(SpringDataSmsGatewayConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SmsGatewayConfig> findActiveConfig() {
        Optional<SmsGatewayConfigEntity> tenantConfig = repository.findFirstByActiveTrueOrderByUpdatedAtDesc();
        if (tenantConfig.isPresent()) {
            return tenantConfig.map(this::toDomain);
        }
        return repository.findMasterFallbackConfig().map(this::toDomain);
    }

    @Override
    public SmsGatewayConfig save(SmsGatewayConfig domain) {
        SmsGatewayConfigEntity entity = toEntity(domain);
        entity.setUpdatedAt(Instant.now());
        SmsGatewayConfigEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private SmsGatewayConfigEntity toEntity(SmsGatewayConfig domain) {
        if (domain == null) return null;
        SmsGatewayConfigEntity entity = new SmsGatewayConfigEntity();
        if (domain.getConfigId() != null && repository.existsById(domain.getConfigId())) {
            entity.setConfigId(domain.getConfigId());
        }
        entity.setProvider(domain.getProvider());
        entity.setSenderId(domain.getSenderId());
        entity.setApiKey(domain.getApiKey());
        entity.setApiSecret(domain.getApiSecret());
        entity.setApiUrl(domain.getApiUrl());
        entity.setServiceAccountId(domain.getServiceAccountId());
        entity.setExtraHeadersJson(domain.getExtraHeadersJson());
        entity.setActive(domain.isActive());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        if (domain.getUpdatedAt() != null) {
            entity.setUpdatedAt(domain.getUpdatedAt());
        }
        return entity;
    }

    private SmsGatewayConfig toDomain(SmsGatewayConfigEntity entity) {
        if (entity == null) return null;
        return new SmsGatewayConfig(
                entity.getConfigId(),
                entity.getProvider(),
                entity.getSenderId(),
                entity.getApiKey(),
                entity.getApiSecret(),
                entity.getApiUrl(),
                entity.getServiceAccountId(),
                entity.getExtraHeadersJson(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
