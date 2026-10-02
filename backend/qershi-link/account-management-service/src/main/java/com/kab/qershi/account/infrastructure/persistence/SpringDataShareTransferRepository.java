package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Share Transfers.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataShareTransferRepository extends JpaRepository<ShareTransferEntity, UUID> {

    List<ShareTransferEntity> findByFromMemberId(UUID fromMemberId);

    List<ShareTransferEntity> findByToMemberId(UUID toMemberId);

    List<ShareTransferEntity> findByStatus(String status);
}
