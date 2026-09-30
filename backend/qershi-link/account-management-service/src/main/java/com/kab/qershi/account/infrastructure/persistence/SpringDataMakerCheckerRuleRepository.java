package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for accessing SACCO Maker-Checker policy rules.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataMakerCheckerRuleRepository extends JpaRepository<MakerCheckerRuleEntity, UUID> {
    Optional<MakerCheckerRuleEntity> findFirstByOrderByCreatedAtAsc();
    Optional<MakerCheckerRuleEntity> findBySaccoCode(String saccoCode);
}
