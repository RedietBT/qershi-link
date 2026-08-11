package com.kab.qershi.auth.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for master_schema.audit_logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    List<AuditLogEntity> findByUserIdOrderByTimestampDesc(UUID userId);

    List<AuditLogEntity> findBySaccoIdOrderByTimestampDesc(UUID saccoId);

    Page<AuditLogEntity> findAllByOrderByTimestampDesc(Pageable pageable);
}
