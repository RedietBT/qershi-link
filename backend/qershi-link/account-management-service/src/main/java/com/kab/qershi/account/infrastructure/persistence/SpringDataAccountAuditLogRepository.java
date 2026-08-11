package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for sacco_xxx.account_audit_logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataAccountAuditLogRepository extends JpaRepository<AccountAuditLogEntity, UUID> {

    List<AccountAuditLogEntity> findByAccountNoOrderByCreatedAtDesc(String accountNo);

    List<AccountAuditLogEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<AccountAuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
