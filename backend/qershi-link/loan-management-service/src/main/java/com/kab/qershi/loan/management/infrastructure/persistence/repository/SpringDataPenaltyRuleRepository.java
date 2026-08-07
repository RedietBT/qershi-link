package com.kab.qershi.loan.management.infrastructure.persistence.repository;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.PenaltyRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for PenaltyRuleEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataPenaltyRuleRepository extends JpaRepository<PenaltyRuleEntity, UUID> {

    Optional<PenaltyRuleEntity> findByPolicyCode(String policyCode);

    List<PenaltyRuleEntity> findByActiveTrue();
}
