package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.ShareTransfer;
import com.kab.qershi.account.domain.ports.outbound.ShareTransferRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.ShareTransferEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataShareTransferRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter bridging ShareTransfer domain model and JPA entity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ShareTransferRepositoryAdapter implements ShareTransferRepositoryPort {

    private final SpringDataShareTransferRepository repository;

    public ShareTransferRepositoryAdapter(SpringDataShareTransferRepository repository) {
        this.repository = repository;
    }

    @Override
    public ShareTransfer save(ShareTransfer domain) {
        ShareTransferEntity entity = toEntity(domain);
        ShareTransferEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ShareTransfer> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ShareTransfer> findByFromMemberId(UUID fromMemberId) {
        return repository.findByFromMemberId(fromMemberId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShareTransfer> findByToMemberId(UUID toMemberId) {
        return repository.findByToMemberId(toMemberId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShareTransfer> findByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private ShareTransfer toDomain(ShareTransferEntity entity) {
        if (entity == null) return null;
        return new ShareTransfer(
                entity.getId(),
                entity.getFromMemberId(),
                entity.getToMemberId(),
                entity.getCertificateId(),
                entity.getShareCount(),
                entity.getTransferPrice(),
                entity.getStatus(),
                entity.getApprovedBy(),
                entity.getApprovedAt(),
                entity.getRejectionReason(),
                entity.getCreatedAt()
        );
    }

    private ShareTransferEntity toEntity(ShareTransfer domain) {
        if (domain == null) return null;
        ShareTransferEntity entity = new ShareTransferEntity();
        entity.setId(domain.getId());
        entity.setFromMemberId(domain.getFromMemberId());
        entity.setToMemberId(domain.getToMemberId());
        entity.setCertificateId(domain.getCertificateId());
        entity.setShareCount(domain.getShareCount());
        entity.setTransferPrice(domain.getTransferPrice());
        entity.setStatus(domain.getStatus());
        entity.setApprovedBy(domain.getApprovedBy());
        entity.setApprovedAt(domain.getApprovedAt());
        entity.setRejectionReason(domain.getRejectionReason());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
