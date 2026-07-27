package com.kab.qershi.profile.domain.ports.inbound;

import com.kab.qershi.profile.domain.model.Gender;
import com.kab.qershi.profile.domain.model.MaritalStatus;
import com.kab.qershi.profile.domain.model.MemberAddress;
import com.kab.qershi.profile.domain.model.MemberEmployment;
import com.kab.qershi.profile.domain.model.MemberProfile;
import com.kab.qershi.profile.domain.model.MemberStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound Port interface defining use cases for core member profile onboarding,
 * demographic updates, Maker-Checker supervisor approvals, and profile queries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ProfileManagementUseCase {

    /**
     * Registers a new member's demographic profile linked to their identity user ID.
     */
    MemberProfile registerMemberProfile(
            UUID userId,
            String memberNo,
            String firstName,
            String middleName,
            String lastName,
            Gender gender,
            LocalDate dateOfBirth,
            MaritalStatus maritalStatus,
            String primaryPhone,
            String secondaryPhone,
            String email,
            String region,
            String zoneSubcity,
            String woreda,
            String houseNumber,
            String occupationSector,
            String employerName,
            BigDecimal monthlyIncome,
            String tinNumber,
            UUID submittedByUserId
    );

    Optional<MemberProfile> getProfileByUserId(UUID userId);

    Optional<MemberProfile> getProfileByMemberNo(String memberNo);

    List<MemberProfile> getAllProfiles(MemberStatus status);

    MemberAddress updateContactAddress(
            UUID userId,
            String primaryPhone,
            String secondaryPhone,
            String email,
            String region,
            String zoneSubcity,
            String woreda,
            String houseNumber
    );

    MemberEmployment updateEmploymentProfile(
            UUID userId,
            String occupationSector,
            String employerName,
            BigDecimal monthlyIncome,
            String tinNumber
    );

    MemberProfile approveMemberOnboarding(UUID userId, UUID supervisorId, String remarks);

    MemberProfile changeMemberStatus(UUID userId, MemberStatus newStatus);
}
