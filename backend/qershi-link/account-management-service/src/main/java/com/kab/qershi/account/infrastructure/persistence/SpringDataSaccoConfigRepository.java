package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for SaccoConfigEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataSaccoConfigRepository extends JpaRepository<SaccoConfigEntity, UUID> {
    Optional<SaccoConfigEntity> findFirstByOrderByCreatedAtAsc();
}
