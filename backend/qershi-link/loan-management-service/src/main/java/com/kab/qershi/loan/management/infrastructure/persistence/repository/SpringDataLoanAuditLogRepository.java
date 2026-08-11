package com.kab.qershi.loan.management.infrastructure.persistence.repository;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for sacco_xxx.account_audit_logs loan events.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanAuditLogRepository extends JpaRepository<LoanAuditLogEntity, UUID> {

    List<LoanAuditLogEntity> findByAccountNoOrderByCreatedAtDesc(String accountNo);

    List<LoanAuditLogEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<LoanAuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
