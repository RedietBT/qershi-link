package com.kab.qershi.profile.domain.ports.outbound;

import com.kab.qershi.profile.domain.model.MemberAddress;
import com.kab.qershi.profile.domain.model.MemberEmployment;
import com.kab.qershi.profile.domain.model.MemberGovernance;
import com.kab.qershi.profile.domain.model.MemberProfile;
import com.kab.qershi.profile.domain.model.MemberStatus;
import com.kab.qershi.profile.domain.model.ProfileAuditLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port interface defining persistence contracts for member profiles,
 * addresses, employments, governance records, and audit logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ProfileRepositoryPort {

    MemberProfile saveProfile(MemberProfile profile);

    Optional<MemberProfile> findByUserId(UUID userId);

    Optional<MemberProfile> findByMemberNo(String memberNo);

    List<MemberProfile> findAllProfiles(MemberStatus status);

    boolean existsByMemberNo(String memberNo);

    MemberAddress saveAddress(MemberAddress address);

    Optional<MemberAddress> findAddressByUserId(UUID userId);

    boolean existsByPrimaryPhone(String primaryPhone);

    MemberEmployment saveEmployment(MemberEmployment employment);

    Optional<MemberEmployment> findEmploymentByUserId(UUID userId);

    MemberGovernance saveGovernance(MemberGovernance governance);

    Optional<MemberGovernance> findGovernanceByUserId(UUID userId);

    void saveAuditLog(ProfileAuditLog log);

    void deleteProfileByUserId(UUID userId);
}
