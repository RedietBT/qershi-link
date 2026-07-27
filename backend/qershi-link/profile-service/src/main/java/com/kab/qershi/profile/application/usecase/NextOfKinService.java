package com.kab.qershi.profile.application.usecase;

import com.kab.qershi.profile.domain.model.NextOfKin;
import com.kab.qershi.profile.domain.model.ProfileAuditLog;
import com.kab.qershi.profile.domain.ports.inbound.NextOfKinUseCase;
import com.kab.qershi.profile.domain.ports.outbound.NextOfKinRepositoryPort;
import com.kab.qershi.profile.domain.ports.outbound.ProfileRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Application Use Case implementation service managing nominated beneficiaries and 100% allocation validations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class NextOfKinService implements NextOfKinUseCase {

    private static final Logger log = LoggerFactory.getLogger(NextOfKinService.class);

    private final NextOfKinRepositoryPort nextOfKinRepository;
    private final ProfileRepositoryPort profileRepository;

    public NextOfKinService(NextOfKinRepositoryPort nextOfKinRepository, ProfileRepositoryPort profileRepository) {
        this.nextOfKinRepository = nextOfKinRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public NextOfKin addNextOfKin(UUID userId, String fullName, String relationship, String primaryPhone,
                                  String idNumber, String physicalAddress, BigDecimal allocationPercentage) {
        log.info("Adding Next of Kin ({}) for user ID: {}", fullName, userId);

        profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot add Next of Kin. Member profile not found for user ID: " + userId));

        List<NextOfKin> existingList = nextOfKinRepository.findByUserId(userId);
        BigDecimal currentSum = existingList.stream()
                .map(NextOfKin::getAllocationPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = currentSum.add(allocationPercentage);
        if (newTotal.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Total allocation percentage cannot exceed 100.00%%. Current total: %s%%, Requested addition: %s%%",
                    currentSum, allocationPercentage
            ));
        }

        NextOfKin kin = new NextOfKin(
                UUID.randomUUID(),
                userId,
                fullName,
                relationship,
                primaryPhone,
                idNumber,
                physicalAddress,
                allocationPercentage
        );

        NextOfKin saved = nextOfKinRepository.saveNextOfKin(kin);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "ADD_NEXT_OF_KIN",
                "fullName",
                null,
                fullName + " (" + allocationPercentage + "%)"
        ));

        return saved;
    }

    @Override
    public List<NextOfKin> getNextOfKinByUserId(UUID userId) {
        return nextOfKinRepository.findByUserId(userId);
    }

    @Override
    public NextOfKin updateNextOfKin(UUID kinId, String fullName, String relationship, String primaryPhone,
                                     String idNumber, String physicalAddress, BigDecimal allocationPercentage) {
        log.info("Updating Next of Kin ID: {}", kinId);

        NextOfKin existing = nextOfKinRepository.findById(kinId)
                .orElseThrow(() -> new IllegalArgumentException("Next of Kin record not found for ID: " + kinId));

        UUID userId = existing.getUserId();
        List<NextOfKin> existingList = nextOfKinRepository.findByUserId(userId);

        BigDecimal otherSum = existingList.stream()
                .filter(k -> !k.getKinId().equals(kinId))
                .map(NextOfKin::getAllocationPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = otherSum.add(allocationPercentage);
        if (newTotal.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Total allocation percentage cannot exceed 100.00%%. Total with updated value: %s%%",
                    newTotal
            ));
        }

        existing.setFullName(fullName);
        existing.setRelationship(relationship);
        existing.setPrimaryPhone(primaryPhone);
        existing.setIdNumber(idNumber);
        existing.setPhysicalAddress(physicalAddress);
        existing.setAllocationPercentage(allocationPercentage);

        NextOfKin updated = nextOfKinRepository.saveNextOfKin(existing);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "UPDATE_NEXT_OF_KIN",
                "allocationPercentage",
                null,
                allocationPercentage.toString()
        ));

        return updated;
    }

    @Override
    public void deleteNextOfKin(UUID kinId) {
        log.warn("Deleting Next of Kin ID: {}", kinId);

        NextOfKin existing = nextOfKinRepository.findById(kinId)
                .orElseThrow(() -> new IllegalArgumentException("Next of Kin record not found for ID: " + kinId));

        UUID userId = existing.getUserId();
        nextOfKinRepository.deleteById(kinId);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "DELETE_NEXT_OF_KIN",
                "fullName",
                existing.getFullName(),
                null
        ));
    }
}
