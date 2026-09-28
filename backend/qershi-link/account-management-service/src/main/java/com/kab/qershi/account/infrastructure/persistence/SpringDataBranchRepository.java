package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for SACCO Branch entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataBranchRepository extends JpaRepository<BranchEntity, UUID> {

    Optional<BranchEntity> findByBranchCode(String branchCode);

    boolean existsByBranchCode(String branchCode);

    List<BranchEntity> findByStatus(String status);
}
