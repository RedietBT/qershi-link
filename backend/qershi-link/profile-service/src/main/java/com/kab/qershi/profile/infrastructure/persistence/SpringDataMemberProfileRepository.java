package com.kab.qershi.profile.infrastructure.persistence;

import com.kab.qershi.profile.domain.model.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for MemberProfileEntity persistence operations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataMemberProfileRepository extends JpaRepository<MemberProfileEntity, UUID> {

    Optional<MemberProfileEntity> findByMemberNo(String memberNo);

    List<MemberProfileEntity> findByStatus(MemberStatus status);

    boolean existsByMemberNo(String memberNo);
}
