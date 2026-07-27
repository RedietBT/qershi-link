package com.kab.qershi.profile.infrastructure.adapters;

import com.kab.qershi.profile.domain.model.MemberIdentification;
import com.kab.qershi.profile.domain.ports.outbound.KycRepositoryPort;
import com.kab.qershi.profile.infrastructure.persistence.MemberIdentificationEntity;
import com.kab.qershi.profile.infrastructure.persistence.SpringDataMemberIdentificationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Persistence Adapter implementing KycRepositoryPort.
 * Translates between pure MemberIdentification Domain Models and JPA Entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class KycRepositoryAdapter implements KycRepositoryPort {

    private final SpringDataMemberIdentificationRepository identificationRepository;

    public KycRepositoryAdapter(SpringDataMemberIdentificationRepository identificationRepository) {
        this.identificationRepository = identificationRepository;
    }

    @Override
    public MemberIdentification saveIdentification(MemberIdentification domain) {
        MemberIdentificationEntity entity = toEntity(domain);
        MemberIdentificationEntity saved = identificationRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MemberIdentification> findById(UUID identificationId) {
        return identificationRepository.findById(identificationId).map(this::toDomain);
    }

    @Override
    public List<MemberIdentification> findByUserId(UUID userId) {
        return identificationRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        identificationRepository.deleteByUserId(userId);
    }

    // --- Domain ⇄ Entity Mappers ---

    private MemberIdentificationEntity toEntity(MemberIdentification domain) {
        if (domain == null) return null;
        return new MemberIdentificationEntity(
                domain.getIdentificationId(),
                domain.getUserId(),
                domain.getIdType(),
                domain.getIdNumber(),
                domain.getIssueDate(),
                domain.getExpiryDate(),
                domain.getIssuingAuthority(),
                domain.getKycStatus(),
                domain.getVerifiedByUserId(),
                domain.getVerificationNotes(),
                domain.getCreatedAt()
        );
    }

    private MemberIdentification toDomain(MemberIdentificationEntity entity) {
        if (entity == null) return null;
        return new MemberIdentification(
                entity.getIdentificationId(),
                entity.getUserId(),
                entity.getIdType(),
                entity.getIdNumber(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.getIssuingAuthority(),
                entity.getKycStatus(),
                entity.getVerifiedByUserId(),
                entity.getVerificationNotes()
        );
    }
}
