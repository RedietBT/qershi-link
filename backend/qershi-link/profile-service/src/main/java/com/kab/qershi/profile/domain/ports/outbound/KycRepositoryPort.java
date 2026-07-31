package com.kab.qershi.profile.domain.ports.outbound;

import com.kab.qershi.profile.domain.model.KycStatus;
import com.kab.qershi.profile.domain.model.MemberIdentification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port interface defining persistence contracts for member KYC identification documents.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface KycRepositoryPort {

    MemberIdentification saveIdentification(MemberIdentification identification);

    Optional<MemberIdentification> findById(UUID identificationId);

    List<MemberIdentification> findByUserId(UUID userId);

    List<MemberIdentification> findAllIdentifications(KycStatus status);

    void deleteByUserId(UUID userId);
}
