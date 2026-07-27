package com.kab.qershi.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for MemberGovernanceEntity persistence operations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataMemberGovernanceRepository extends JpaRepository<MemberGovernanceEntity, UUID> {

    Optional<MemberGovernanceEntity> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
