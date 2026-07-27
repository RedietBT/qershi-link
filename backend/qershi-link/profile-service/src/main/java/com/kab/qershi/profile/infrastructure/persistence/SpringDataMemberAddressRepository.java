package com.kab.qershi.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for MemberAddressEntity persistence operations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataMemberAddressRepository extends JpaRepository<MemberAddressEntity, UUID> {

    Optional<MemberAddressEntity> findByUserId(UUID userId);

    boolean existsByPrimaryPhone(String primaryPhone);

    void deleteByUserId(UUID userId);
}
