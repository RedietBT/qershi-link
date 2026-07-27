package com.kab.qershi.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ProfileAuditLogEntity persistence operations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataProfileAuditLogRepository extends JpaRepository<ProfileAuditLogEntity, UUID> {

    List<ProfileAuditLogEntity> findByUserId(UUID userId);
}
