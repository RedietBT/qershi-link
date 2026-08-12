package com.kab.qershi.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository interface for NotificationTemplateEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataNotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, UUID> {

    Optional<NotificationTemplateEntity> findByTemplateCode(String templateCode);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM master_schema.notification_templates WHERE template_code = :templateCode AND is_active = true LIMIT 1", nativeQuery = true)
    Optional<NotificationTemplateEntity> findMasterFallbackTemplate(@org.springframework.data.repository.query.Param("templateCode") String templateCode);

    boolean existsByTemplateCode(String templateCode);
}
