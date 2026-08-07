package com.kab.qershi.loan.origination.infrastructure.persistence.adapter;

import com.kab.qershi.loan.origination.domain.model.GroupMember;
import com.kab.qershi.loan.origination.domain.model.LoanGroup;
import com.kab.qershi.loan.origination.domain.ports.outbound.LoanGroupRepositoryPort;
import com.kab.qershi.loan.origination.infrastructure.persistence.entity.LoanGroupEntity;
import com.kab.qershi.loan.origination.infrastructure.persistence.entity.LoanGroupMemberEntity;
import com.kab.qershi.loan.origination.infrastructure.persistence.repository.SpringDataLoanGroupRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound JPA repository adapter implementing LoanGroupRepositoryPort.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanGroupRepositoryAdapter implements LoanGroupRepositoryPort {

    private final SpringDataLoanGroupRepository repository;

    public LoanGroupRepositoryAdapter(SpringDataLoanGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoanGroup save(LoanGroup group) {
        LoanGroupEntity entity = toEntity(group);
        LoanGroupEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LoanGroup> findById(UUID groupId) {
        return repository.findById(groupId).map(this::toDomain);
    }

    @Override
    public boolean existsByLicenseNo(String licenseNo) {
        if (licenseNo == null || licenseNo.isBlank()) return false;
        return repository.existsByLicenseNo(licenseNo);
    }

    @Override
    public List<LoanGroup> findAll() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private LoanGroupEntity toEntity(LoanGroup domain) {
        List<LoanGroupMemberEntity> memberEntities = domain.getMembers().stream()
                .map(m -> new LoanGroupMemberEntity(domain.getGroupId(), m.getUserId(), m.isLeader(), m.getJoinedAt()))
                .collect(Collectors.toList());

        return new LoanGroupEntity(
                domain.getGroupId(),
                domain.getGroupName(),
                domain.isFormal(),
                domain.getLicenseNo(),
                memberEntities,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private LoanGroup toDomain(LoanGroupEntity entity) {
        List<GroupMember> members = entity.getMembers().stream()
                .map(m -> new GroupMember(entity.getGroupId(), m.getUserId(), m.isLeader(), m.getJoinedAt()))
                .collect(Collectors.toList());

        return new LoanGroup(
                entity.getGroupId(),
                entity.getGroupName(),
                entity.isFormal(),
                entity.getLicenseNo(),
                members,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
