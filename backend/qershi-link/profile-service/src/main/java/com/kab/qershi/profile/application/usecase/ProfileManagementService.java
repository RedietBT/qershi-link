package com.kab.qershi.profile.application.usecase;

import com.kab.qershi.profile.domain.model.Gender;
import com.kab.qershi.profile.domain.model.MaritalStatus;
import com.kab.qershi.profile.domain.model.MemberAddress;
import com.kab.qershi.profile.domain.model.MemberEmployment;
import com.kab.qershi.profile.domain.model.MemberGovernance;
import com.kab.qershi.profile.domain.model.MemberProfile;
import com.kab.qershi.profile.domain.model.MemberStatus;
import com.kab.qershi.profile.domain.model.ProfileAuditLog;
import com.kab.qershi.profile.domain.ports.inbound.ProfileManagementUseCase;
import com.kab.qershi.profile.domain.ports.outbound.KycRepositoryPort;
import com.kab.qershi.profile.domain.ports.outbound.NextOfKinRepositoryPort;
import com.kab.qershi.profile.domain.ports.outbound.ProfileRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Application Use Case implementation service managing member profile lifecycles,
 * demographic updates, Maker-Checker onboarding approvals, and cascade purges.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
public class ProfileManagementService implements ProfileManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProfileManagementService.class);

    private final ProfileRepositoryPort profileRepository;
    private final KycRepositoryPort kycRepository;
    private final NextOfKinRepositoryPort nextOfKinRepository;
    private final Random random = new Random();

    public ProfileManagementService(ProfileRepositoryPort profileRepository,
                                    KycRepositoryPort kycRepository,
                                    NextOfKinRepositoryPort nextOfKinRepository) {
        this.profileRepository = profileRepository;
        this.kycRepository = kycRepository;
        this.nextOfKinRepository = nextOfKinRepository;
    }

    @Override
    public MemberProfile createMemberProfile(UUID userId, String firstName, String middleName,
                                             String lastName, Gender gender, LocalDate dateOfBirth,
                                             MaritalStatus maritalStatus, UUID submittedByUserId) {
        log.info("Initiating tenant-scoped member profile creation for user ID: {}", userId);

        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Profile already exists for user ID: " + userId);
        }

        // Auto-generate structured Member ID in format: QL-[YEAR]-[6_DIGIT_SEQUENCE] (e.g. QL-2026-000142)
        String formattedMemberNo = generateStructuredMemberNo(null);

        // Guarantee unique member number constraint safety
        int attempts = 0;
        while (profileRepository.existsByMemberNo(formattedMemberNo)) {
            attempts++;
            formattedMemberNo = generateStructuredMemberNo(null);
            if (attempts > 10) {
                throw new IllegalStateException("Failed to generate unique member number after multiple attempts.");
            }
        }

        MemberProfile profile = new MemberProfile(
                userId,
                formattedMemberNo,
                firstName,
                middleName,
                lastName,
                gender,
                dateOfBirth,
                maritalStatus,
                MemberStatus.PENDING_APPROVAL
        );

        MemberProfile savedProfile = profileRepository.saveProfile(profile);

        // Record Maker Governance Onboarding Application
        MemberGovernance governance = new MemberGovernance(
                UUID.randomUUID(),
                userId,
                submittedByUserId != null ? submittedByUserId : userId,
                null,
                null,
                "Onboarding profile application initiated."
        );
        profileRepository.saveGovernance(governance);

        // Emit Audit Trail Log
        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                submittedByUserId != null ? submittedByUserId : userId,
                "CREATE_MEMBER_PROFILE",
                "status",
                null,
                MemberStatus.PENDING_APPROVAL.name()
        ));

        log.info("Member profile created successfully with Member No: {} for user ID: {}", formattedMemberNo, userId);
        return savedProfile;
    }

    @Override
    public MemberProfile updateDemographics(UUID userId, String firstName, String middleName, String lastName,
                                           Gender gender, LocalDate dateOfBirth, MaritalStatus maritalStatus) {
        log.info("Updating tenant-scoped demographics for user ID: {}", userId);

        MemberProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Member profile not found for user ID: " + userId));

        profile.setFirstName(firstName);
        profile.setMiddleName(middleName);
        profile.setLastName(lastName);
        profile.setGender(gender);
        profile.setDateOfBirth(dateOfBirth);
        profile.setMaritalStatus(maritalStatus);
        profile.updateTimestamp();

        MemberProfile updated = profileRepository.saveProfile(profile);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "UPDATE_DEMOGRAPHICS",
                "fullName",
                null,
                profile.getFullName()
        ));

        return updated;
    }

    @Override
    public MemberAddress saveContactAddress(UUID userId, String primaryPhone, String secondaryPhone, String email,
                                            String region, String zoneSubcity, String woreda, String houseNumber) {
        log.info("Saving contact address for user ID: {}", userId);

        profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot set address. Member profile not found for user ID: " + userId));

        Optional<MemberAddress> existingOpt = profileRepository.findAddressByUserId(userId);
        UUID addressId = existingOpt.map(MemberAddress::getAddressId).orElseGet(UUID::randomUUID);

        MemberAddress address = new MemberAddress(
                addressId,
                userId,
                primaryPhone,
                secondaryPhone,
                email,
                region,
                zoneSubcity,
                woreda,
                houseNumber
        );

        MemberAddress savedAddress = profileRepository.saveAddress(address);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "SAVE_CONTACT_ADDRESS",
                "primaryPhone",
                existingOpt.map(MemberAddress::getPrimaryPhone).orElse(null),
                primaryPhone
        ));

        return savedAddress;
    }

    @Override
    public MemberEmployment saveEmploymentProfile(UUID userId, String occupationSector, String employerName,
                                                   BigDecimal monthlyIncome, String tinNumber) {
        log.info("Saving employment profile for user ID: {}", userId);

        profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot set employment. Member profile not found for user ID: " + userId));

        Optional<MemberEmployment> existingOpt = profileRepository.findEmploymentByUserId(userId);
        UUID employmentId = existingOpt.map(MemberEmployment::getEmploymentId).orElseGet(UUID::randomUUID);

        MemberEmployment employment = new MemberEmployment(
                employmentId,
                userId,
                occupationSector,
                employerName,
                monthlyIncome,
                tinNumber
        );

        MemberEmployment savedEmployment = profileRepository.saveEmployment(employment);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "SAVE_EMPLOYMENT_PROFILE",
                "occupationSector",
                existingOpt.map(MemberEmployment::getOccupationSector).orElse(null),
                occupationSector
        ));

        return savedEmployment;
    }

    @Override
    public MemberProfile approveMemberOnboarding(UUID userId, UUID supervisorId, String remarks) {
        log.info("Supervisor {} approving member onboarding for user ID: {}", supervisorId, userId);

        MemberProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Member profile not found for user ID: " + userId));

        MemberGovernance governance = profileRepository.findGovernanceByUserId(userId)
                .orElseGet(() -> new MemberGovernance(UUID.randomUUID(), userId, userId, null, null, null));

        governance.approve(supervisorId, remarks);
        profileRepository.saveGovernance(governance);

        profile.setStatus(MemberStatus.ACTIVE);
        profile.updateTimestamp();
        MemberProfile approvedProfile = profileRepository.saveProfile(profile);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                supervisorId,
                "APPROVE_MEMBER_ONBOARDING",
                "status",
                MemberStatus.PENDING_APPROVAL.name(),
                MemberStatus.ACTIVE.name()
        ));

        log.info("Member onboarding approved successfully for user ID: {}", userId);
        return approvedProfile;
    }

    @Override
    public MemberProfile changeMemberStatus(UUID userId, MemberStatus newStatus) {
        log.info("Changing status for user ID: {} to {}", userId, newStatus);

        MemberProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Member profile not found for user ID: " + userId));

        MemberStatus oldStatus = profile.getStatus();
        profile.setStatus(newStatus);
        profile.updateTimestamp();

        MemberProfile updated = profileRepository.saveProfile(profile);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "CHANGE_MEMBER_STATUS",
                "status",
                oldStatus.name(),
                newStatus.name()
        ));

        return updated;
    }

    @Override
    public Optional<MemberProfile> getProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId);
    }

    @Override
    public Optional<MemberProfile> getProfileByMemberNo(String memberNo) {
        return profileRepository.findByMemberNo(memberNo);
    }

    @Override
    public List<MemberProfile> getAllProfiles(MemberStatus status) {
        return profileRepository.findAllProfiles(status);
    }

    @Override
    public void deleteProfileByUserId(UUID userId) {
        log.warn("Executing cascade deletion for member profile with user ID: {}", userId);
        kycRepository.deleteByUserId(userId);
        nextOfKinRepository.deleteByUserId(userId);
        profileRepository.deleteProfileByUserId(userId);

        profileRepository.saveAuditLog(new ProfileAuditLog(
                UUID.randomUUID(),
                userId,
                userId,
                "CASCADE_DELETE_PROFILE",
                "all",
                "ACTIVE",
                "DELETED"
        ));
    }

    /**
     * Generates a structured Member ID in format: [INITIALS]-[YEAR]-[6_DIGIT_SEQUENCE]
     * Examples: QL-2026-000142, AWS-2026-100589
     */
    private String generateStructuredMemberNo(String saccoName) {
        String initials = deriveSaccoInitials(saccoName);
        int year = Year.now().getValue();
        int randomSeq = random.nextInt(900000) + 100000; // 6-digit sequence (100000-999999)
        return String.format("%s-%d-%06d", initials, year, randomSeq);
    }

    /**
     * Helper to derive 2 to 3 uppercase initials from a SACCO legal name.
     */
    public static String deriveSaccoInitials(String saccoName) {
        if (saccoName == null || saccoName.trim().isEmpty()) {
            return "QL";
        }
        String[] words = saccoName.trim().toUpperCase().split("\\s+");
        if (words.length == 1) {
            String w = words[0];
            return w.length() >= 3 ? w.substring(0, 3) : w;
        }
        if (words.length == 2) {
            String w1 = words[0];
            String w2 = words[1];
            if (w1.length() >= 2) {
                return "" + w1.charAt(0) + w1.charAt(1) + w2.charAt(0);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty() && Character.isLetter(word.charAt(0))) {
                sb.append(word.charAt(0));
                if (sb.length() >= 3) break;
            }
        }
        return sb.length() > 0 ? sb.toString() : "QL";
    }
}
