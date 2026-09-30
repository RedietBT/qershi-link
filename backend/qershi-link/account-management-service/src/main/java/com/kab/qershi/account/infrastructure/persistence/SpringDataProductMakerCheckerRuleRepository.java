package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for per-product Maker-Checker rules.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataProductMakerCheckerRuleRepository extends JpaRepository<ProductMakerCheckerRuleEntity, UUID> {
    Optional<ProductMakerCheckerRuleEntity> findByProductCode(String productCode);
}
