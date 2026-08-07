package com.kab.qershi.loan.management.infrastructure.persistence.repository;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.RepaymentScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for RepaymentScheduleEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataRepaymentScheduleRepository extends JpaRepository<RepaymentScheduleEntity, UUID> {

    List<RepaymentScheduleEntity> findByAccountIdOrderByInstallmentNoAsc(UUID accountId);
}
