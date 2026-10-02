package com.kab.qershi.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for SmsGatewayConfigEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataSmsGatewayConfigRepository extends JpaRepository<SmsGatewayConfigEntity, UUID> {

    Optional<SmsGatewayConfigEntity> findFirstByActiveTrueOrderByUpdatedAtDesc();

    @Query(value = "SELECT * FROM master_schema.sms_gateway_configs WHERE is_active = true ORDER BY updated_at DESC LIMIT 1", nativeQuery = true)
    Optional<SmsGatewayConfigEntity> findMasterFallbackConfig();
}
