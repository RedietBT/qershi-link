package com.kab.qershi.transaction.infrastructure.persistence;

import com.kab.qershi.transaction.domain.model.TillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository interface for 'teller_tills' table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataTellerTillRepository extends JpaRepository<TellerTillEntity, UUID> {

    Optional<TellerTillEntity> findByTellerUserId(UUID tellerUserId);

    Optional<TellerTillEntity> findByTellerUserIdAndStatus(UUID tellerUserId, TillStatus status);

    List<TellerTillEntity> findByBranchId(UUID branchId);

    List<TellerTillEntity> findByBranchCode(String branchCode);

    List<TellerTillEntity> findByBranchIdAndStatus(UUID branchId, TillStatus status);
}
