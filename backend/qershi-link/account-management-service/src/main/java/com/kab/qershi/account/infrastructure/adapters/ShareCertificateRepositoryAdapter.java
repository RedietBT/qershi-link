package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.ShareCertificate;
import com.kab.qershi.account.domain.ports.outbound.ShareCertificateRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.ShareCertificateEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataShareCertificateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter bridging ShareCertificate domain model and JPA entity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ShareCertificateRepositoryAdapter implements ShareCertificateRepositoryPort {

    private final SpringDataShareCertificateRepository repository;

    public ShareCertificateRepositoryAdapter(SpringDataShareCertificateRepository repository) {
        this.repository = repository;
    }

    @Override
    public ShareCertificate save(ShareCertificate domain) {
        ShareCertificateEntity entity = toEntity(domain);
        ShareCertificateEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ShareCertificate> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ShareCertificate> findByShareAccountId(UUID shareAccountId) {
        return repository.findByShareAccountId(shareAccountId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShareCertificate> findActiveByShareAccountId(UUID shareAccountId) {
        return repository.findByShareAccountIdAndStatus(shareAccountId, "ACTIVE").stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ShareCertificate> findByCertificateNumber(String certificateNumber) {
        return repository.findByCertificateNumber(certificateNumber).map(this::toDomain);
    }

    @Override
    public Long getNextSerial() {
        return repository.getNextSerial();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private ShareCertificate toDomain(ShareCertificateEntity entity) {
        if (entity == null) return null;
        return new ShareCertificate(
                entity.getId(),
                entity.getShareAccountId(),
                entity.getCertificateNumber(),
                entity.getStartSerial(),
                entity.getEndSerial(),
                entity.getShareCount(),
                entity.getIssueDate(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private ShareCertificateEntity toEntity(ShareCertificate domain) {
        if (domain == null) return null;
        ShareCertificateEntity entity = new ShareCertificateEntity();
        entity.setId(domain.getId());
        entity.setShareAccountId(domain.getShareAccountId());
        entity.setCertificateNumber(domain.getCertificateNumber());
        entity.setStartSerial(domain.getStartSerial());
        entity.setEndSerial(domain.getEndSerial());
        entity.setShareCount(domain.getShareCount());
        entity.setIssueDate(domain.getIssueDate());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
