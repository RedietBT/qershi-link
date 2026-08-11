package com.kab.qershi.profile.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for sacco_xxx.profile_audit_logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataProfileAuditLogRepository extends JpaRepository<ProfileAuditLogEntity, UUID> {

    List<ProfileAuditLogEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<ProfileAuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
