package com.kab.qershi.transaction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTillClosingLogRepository extends JpaRepository<TillClosingLogEntity, UUID> {
    List<TillClosingLogEntity> findByTillIdOrderByCreatedAtDesc(UUID tillId);
    List<TillClosingLogEntity> findByTellerUserIdOrderByCreatedAtDesc(UUID tellerUserId);
}
