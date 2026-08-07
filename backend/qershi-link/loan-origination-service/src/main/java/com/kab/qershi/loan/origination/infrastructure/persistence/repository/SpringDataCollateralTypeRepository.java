package com.kab.qershi.loan.origination.infrastructure.persistence.repository;

import com.kab.qershi.loan.origination.infrastructure.persistence.entity.CollateralTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for collateral_types table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataCollateralTypeRepository extends JpaRepository<CollateralTypeEntity, UUID> {

    Optional<CollateralTypeEntity> findByTypeCode(String typeCode);

    boolean existsByTypeCode(String typeCode);
}
