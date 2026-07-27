package com.kab.qershi.profile.infrastructure.adapters;

import com.kab.qershi.profile.domain.model.MemberAddress;
import com.kab.qershi.profile.domain.model.MemberEmployment;
import com.kab.qershi.profile.domain.model.MemberGovernance;
import com.kab.qershi.profile.domain.model.MemberProfile;
import com.kab.qershi.profile.domain.model.MemberStatus;
import com.kab.qershi.profile.domain.model.ProfileAuditLog;
import com.kab.qershi.profile.domain.ports.outbound.ProfileRepositoryPort;
import com.kab.qershi.profile.infrastructure.persistence.MemberAddressEntity;
import com.kab.qershi.profile.infrastructure.persistence.MemberEmploymentEntity;
import com.kab.qershi.profile.infrastructure.persistence.MemberGovernanceEntity;
import com.kab.qershi.profile.infrastructure.persistence.MemberProfileEntity;
import com.kab.qershi.profile.infrastructure.persistence.ProfileAuditLogEntity;
import com.kab.qershi.profile.infrastructure.persistence.SpringDataMemberAddressRepository;
import com.kab.qershi.profile.infrastructure.persistence.SpringDataMemberEmploymentRepository;
import com.kab.qershi.profile.infrastructure.persistence.SpringDataMemberGovernanceRepository;
import com.kab.qershi.profile.infrastructure.persistence.SpringDataMemberProfileRepository;
import com.kab.qershi.profile.infrastructure.persistence.SpringDataProfileAuditLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Persistence Adapter implementing ProfileRepositoryPort.
 * Translates between pure Domain Models and JPA Entities for database interactions.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ProfileRepositoryAdapter implements ProfileRepositoryPort {

    private final SpringDataMemberProfileRepository profileRepository;
    private final SpringDataMemberAddressRepository addressRepository;
    private final SpringDataMemberEmploymentRepository employmentRepository;
    private final SpringDataMemberGovernanceRepository governanceRepository;
    private final SpringDataProfileAuditLogRepository auditLogRepository;

    public ProfileRepositoryAdapter(SpringDataMemberProfileRepository profileRepository,
                                  SpringDataMemberAddressRepository addressRepository,
                                  SpringDataMemberEmploymentRepository employmentRepository,
                                  SpringDataMemberGovernanceRepository governanceRepository,
                                  SpringDataProfileAuditLogRepository auditLogRepository) {
        this.profileRepository = profileRepository;
        this.addressRepository = addressRepository;
        this.employmentRepository = employmentRepository;
        this.governanceRepository = governanceRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public MemberProfile saveProfile(MemberProfile domain) {
        MemberProfileEntity entity = toEntity(domain);
        MemberProfileEntity saved = profileRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MemberProfile> findByUserId(UUID userId) {
        return profileRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public Optional<MemberProfile> findByMemberNo(String memberNo) {
        return profileRepository.findByMemberNo(memberNo).map(this::toDomain);
    }

    @Override
    public List<MemberProfile> findAllProfiles(MemberStatus status) {
        List<MemberProfileEntity> list = (status != null)
                ? profileRepository.findByStatus(status)
                : profileRepository.findAll();
        return list.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByMemberNo(String memberNo) {
        return profileRepository.existsByMemberNo(memberNo);
    }

    @Override
    public boolean existsByPrimaryPhone(String primaryPhone) {
        return addressRepository.existsByPrimaryPhone(primaryPhone);
    }

    @Override
    public void deleteProfileByUserId(UUID userId) {
        addressRepository.deleteByUserId(userId);
        employmentRepository.deleteByUserId(userId);
        governanceRepository.deleteByUserId(userId);
        profileRepository.deleteById(userId);
    }

    @Override
    public MemberAddress saveAddress(MemberAddress domain) {
        MemberAddressEntity entity = toEntity(domain);
        MemberAddressEntity saved = addressRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MemberAddress> findAddressByUserId(UUID userId) {
        return addressRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public MemberEmployment saveEmployment(MemberEmployment domain) {
        MemberEmploymentEntity entity = toEntity(domain);
        MemberEmploymentEntity saved = employmentRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MemberEmployment> findEmploymentByUserId(UUID userId) {
        return employmentRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public MemberGovernance saveGovernance(MemberGovernance domain) {
        MemberGovernanceEntity entity = toEntity(domain);
        MemberGovernanceEntity saved = governanceRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MemberGovernance> findGovernanceByUserId(UUID userId) {
        return governanceRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public void saveAuditLog(ProfileAuditLog domain) {
        ProfileAuditLogEntity entity = toEntity(domain);
        auditLogRepository.save(entity);
    }

    // --- Domain ⇄ Entity Mappers ---

    private MemberProfileEntity toEntity(MemberProfile domain) {
        if (domain == null) return null;
        return new MemberProfileEntity(
                domain.getUserId(),
                domain.getMemberNo(),
                domain.getFirstName(),
                domain.getMiddleName(),
                domain.getLastName(),
                domain.getGender(),
                domain.getDateOfBirth(),
                domain.getMaritalStatus(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private MemberProfile toDomain(MemberProfileEntity entity) {
        if (entity == null) return null;
        return new MemberProfile(
                entity.getUserId(),
                entity.getMemberNo(),
                entity.getFirstName(),
                entity.getMiddleName(),
                entity.getLastName(),
                entity.getGender(),
                entity.getDateOfBirth(),
                entity.getMaritalStatus(),
                entity.getStatus()
        );
    }

    private MemberAddressEntity toEntity(MemberAddress domain) {
        if (domain == null) return null;
        return new MemberAddressEntity(
                domain.getAddressId(),
                domain.getUserId(),
                domain.getPrimaryPhone(),
                domain.getSecondaryPhone(),
                domain.getEmail(),
                domain.getRegion(),
                domain.getZoneSubcity(),
                domain.getWoreda(),
                domain.getHouseNumber()
        );
    }

    private MemberAddress toDomain(MemberAddressEntity entity) {
        if (entity == null) return null;
        return new MemberAddress(
                entity.getAddressId(),
                entity.getUserId(),
                entity.getPrimaryPhone(),
                entity.getSecondaryPhone(),
                entity.getEmail(),
                entity.getRegion(),
                entity.getZoneSubcity(),
                entity.getWoreda(),
                entity.getHouseNumber()
        );
    }

    private MemberEmploymentEntity toEntity(MemberEmployment domain) {
        if (domain == null) return null;
        return new MemberEmploymentEntity(
                domain.getEmploymentId(),
                domain.getUserId(),
                domain.getOccupationSector(),
                domain.getEmployerName(),
                domain.getMonthlyIncome(),
                domain.getTinNumber()
        );
    }

    private MemberEmployment toDomain(MemberEmploymentEntity entity) {
        if (entity == null) return null;
        return new MemberEmployment(
                entity.getEmploymentId(),
                entity.getUserId(),
                entity.getOccupationSector(),
                entity.getEmployerName(),
                entity.getMonthlyIncome(),
                entity.getTinNumber()
        );
    }

    private MemberGovernanceEntity toEntity(MemberGovernance domain) {
        if (domain == null) return null;
        return new MemberGovernanceEntity(
                domain.getGovernanceId(),
                domain.getUserId(),
                domain.getSubmittedByUserId(),
                domain.getApprovedByUserId(),
                domain.getApprovalDate(),
                domain.getRemarks()
        );
    }

    private MemberGovernance toDomain(MemberGovernanceEntity entity) {
        if (entity == null) return null;
        return new MemberGovernance(
                entity.getGovernanceId(),
                entity.getUserId(),
                entity.getSubmittedByUserId(),
                entity.getApprovedByUserId(),
                entity.getApprovalDate(),
                entity.getRemarks()
        );
    }

    private ProfileAuditLogEntity toEntity(ProfileAuditLog domain) {
        if (domain == null) return null;
        return new ProfileAuditLogEntity(
                domain.getLogId(),
                domain.getUserId(),
                domain.getModifiedByUserId(),
                domain.getAction(),
                domain.getFieldChanged(),
                domain.getOldValue(),
                domain.getNewValue(),
                domain.getTimestamp()
        );
    }

    private ProfileAuditLog toDomain(ProfileAuditLogEntity entity) {
        if (entity == null) return null;
        return new ProfileAuditLog(
                entity.getLogId(),
                entity.getUserId(),
                entity.getModifiedByUserId(),
                entity.getAction(),
                entity.getFieldChanged(),
                entity.getOldValue(),
                entity.getNewValue()
        );
    }
}
