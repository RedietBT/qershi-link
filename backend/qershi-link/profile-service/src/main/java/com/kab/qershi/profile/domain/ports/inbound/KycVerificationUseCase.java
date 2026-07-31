package com.kab.qershi.profile.domain.ports.inbound;

import com.kab.qershi.profile.domain.model.IdType;
import com.kab.qershi.profile.domain.model.KycStatus;
import com.kab.qershi.profile.domain.model.MemberIdentification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound Port interface defining use cases for submitting government identity documents and KYC verification sign-offs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface KycVerificationUseCase {

    MemberIdentification submitKycIdentification(
            UUID userId,
            IdType idType,
            String idNumber,
            LocalDate issueDate,
            LocalDate expiryDate,
            String issuingAuthority
    );

    List<MemberIdentification> getIdentificationsByUserId(UUID userId);

    List<MemberIdentification> getAllIdentifications(KycStatus status);

    Optional<MemberIdentification> getIdentificationById(UUID identificationId);

    MemberIdentification verifyKycIdentification(UUID identificationId, UUID supervisorId, String notes);

    MemberIdentification rejectKycIdentification(UUID identificationId, UUID supervisorId, String notes);
}
