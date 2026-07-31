package com.kab.qershi.profile.application.usecase;

import com.kab.qershi.common.util.PiiMasker;
import com.kab.qershi.profile.domain.model.IdType;
import com.kab.qershi.profile.domain.model.KycStatus;
import com.kab.qershi.profile.domain.model.MemberIdentification;
import com.kab.qershi.profile.domain.model.ProfileAuditLog;
import com.kab.qershi.profile.domain.ports.inbound.KycVerificationUseCase;
import com.kab.qershi.profile.domain.ports.outbound.KycRepositoryPort;
import com.kab.qershi.profile.domain.ports.outbound.ProfileRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Use Case implementation service managing government identity document submissions and KYC verifications.
 * All status changes are audited into profile_audit_logs, and log outputs are PII masked.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class KycVerificationService implements KycVerificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(KycVerificationService.class);

    private final KycRepositoryPort kycRepository;
    private final ProfileRepositoryPort profileRepository;

    public KycVerificationService(KycRepositoryPort kycRepository, ProfileRepositoryPort profileRepository) {
        this.kycRepository = kycRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public MemberIdentification submitKycIdentification(UUID userId, IdType idType, String idNumber,
                                                        LocalDate issueDate, LocalDate expiryDate,
                                                        String issuingAuthority) {
        log.info("Submitting KYC identification document ({}) for user ID: {}", idType, PiiMasker.maskIdNumber(userId.toString()));

        profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot submit KYC. Member profile not found for user ID: " + userId));

        MemberIdentification identification = new MemberIdentification(
                UUID.randomUUID(),
                userId,
                idType,
                idNumber,
                issueDate,
                expiryDate,
                issuingAuthority,
                KycStatus.UNVERIFIED,
                null,
                "Document submitted for review."
        );

        MemberIdentification saved = kycRepository.saveIdentification(identification);

        // Audit Log Entry for KYC Document Submission
        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "SUBMIT_KYC_DOCUMENT",
                "idType",
                null,
                idType.name() + " (" + PiiMasker.maskIdNumber(idNumber) + ")"
        ));

        return saved;
    }

    @Override
    public List<MemberIdentification> getIdentificationsByUserId(UUID userId) {
        return kycRepository.findByUserId(userId);
    }

    @Override
    public List<MemberIdentification> getAllIdentifications(KycStatus status) {
        return kycRepository.findAllIdentifications(status);
    }

    @Override
    public Optional<MemberIdentification> getIdentificationById(UUID identificationId) {
        return kycRepository.findById(identificationId);
    }

    @Override
    public MemberIdentification verifyKycIdentification(UUID identificationId, UUID supervisorId, String notes) {
        log.info("Supervisor {} verifying KYC document ID: {}", PiiMasker.maskIdNumber(supervisorId.toString()), PiiMasker.maskIdNumber(identificationId.toString()));

        MemberIdentification identification = kycRepository.findById(identificationId)
                .orElseThrow(() -> new IllegalArgumentException("Identification document not found for ID: " + identificationId));

        identification.verify(supervisorId, notes);
        MemberIdentification updated = kycRepository.saveIdentification(identification);

        // Audit Log Entry for KYC Document Verification
        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                identification.getUserId(),
                supervisorId,
                "VERIFY_KYC_DOCUMENT",
                "kycStatus",
                KycStatus.UNVERIFIED.name(),
                KycStatus.VERIFIED.name()
        ));

        return updated;
    }

    @Override
    public MemberIdentification rejectKycIdentification(UUID identificationId, UUID supervisorId, String notes) {
        log.warn("Supervisor {} rejecting KYC document ID: {}", PiiMasker.maskIdNumber(supervisorId.toString()), PiiMasker.maskIdNumber(identificationId.toString()));

        MemberIdentification identification = kycRepository.findById(identificationId)
                .orElseThrow(() -> new IllegalArgumentException("Identification document not found for ID: " + identificationId));

        identification.reject(supervisorId, notes);
        MemberIdentification updated = kycRepository.saveIdentification(identification);

        // Audit Log Entry for KYC Document Rejection
        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                identification.getUserId(),
                supervisorId,
                "REJECT_KYC_DOCUMENT",
                "kycStatus",
                KycStatus.UNVERIFIED.name(),
                KycStatus.REJECTED.name()
        ));

        return updated;
    }
}
