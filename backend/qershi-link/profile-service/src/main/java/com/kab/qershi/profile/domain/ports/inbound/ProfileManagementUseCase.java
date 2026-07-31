package com.kab.qershi.profile.domain.ports.inbound;

import com.kab.qershi.profile.domain.model.Gender;
import com.kab.qershi.profile.domain.model.MaritalStatus;
import com.kab.qershi.profile.domain.model.MemberAddress;
import com.kab.qershi.profile.domain.model.MemberEmployment;
import com.kab.qershi.profile.domain.model.MemberGovernance;
import com.kab.qershi.profile.domain.model.MemberProfile;
import com.kab.qershi.profile.domain.model.MemberStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound Port interface defining use cases for modular member profile onboarding,
 * demographic updates, Maker-Checker supervisor approvals, queries, and deletion.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
public interface ProfileManagementUseCase {

    /**
     * Step 1: Creates initial demographic profile and governance maker record.
     * Member number is automatically generated as [INITIALS]-[YEAR]-[6_DIGIT_SEQ] (e.g. QL-2026-000142).
     */
    MemberProfile createMemberProfile(
            UUID userId,
            String firstName,
            String middleName,
            String lastName,
            Gender gender,
            LocalDate dateOfBirth,
            MaritalStatus maritalStatus,
            UUID submittedByUserId
    );

    /**
     * Updates core demographic names, gender, date of birth, and marital status.
     */
    MemberProfile updateDemographics(
            UUID userId,
            String firstName,
            String middleName,
            String lastName,
            Gender gender,
            LocalDate dateOfBirth,
            MaritalStatus maritalStatus
    );

    /**
     * Step 2: Saves or updates contact handle and residence address.
     */
    MemberAddress saveContactAddress(
            UUID userId,
            String primaryPhone,
            String secondaryPhone,
            String email,
            String region,
            String zoneSubcity,
            String woreda,
            String houseNumber
    );

    /**
     * Step 3: Saves or updates occupation, employer, and tax information.
     */
    MemberEmployment saveEmploymentProfile(
            UUID userId,
            String occupationSector,
            String employerName,
            BigDecimal monthlyIncome,
            String tinNumber
    );

    /**
     * Step 6: Four-Eye Checker supervisor onboarding approval sign-off.
     */
    MemberProfile approveMemberOnboarding(UUID userId, UUID supervisorId, String remarks);

    /**
     * Transitions member status (ACTIVE, SUSPENDED, DECEASED, CLOSED).
     */
    MemberProfile changeMemberStatus(UUID userId, MemberStatus newStatus);

    Optional<MemberProfile> getProfileByUserId(UUID userId);

    Optional<MemberProfile> getProfileByMemberNo(String memberNo);

    List<MemberProfile> getAllProfiles(MemberStatus status);

    Optional<MemberAddress> findAddressByUserId(UUID userId);

    Optional<MemberEmployment> findEmploymentByUserId(UUID userId);

    Optional<MemberGovernance> findGovernanceByUserId(UUID userId);

    /**
     * Purges member profile and all dependent records (used by gRPC cascade deletion).
     */
    void deleteProfileByUserId(UUID userId);
}
