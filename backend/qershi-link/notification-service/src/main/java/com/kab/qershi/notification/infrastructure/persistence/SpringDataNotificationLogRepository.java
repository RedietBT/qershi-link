package com.kab.qershi.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository interface for NotificationLogEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataNotificationLogRepository extends JpaRepository<NotificationLogEntity, UUID> {

    List<NotificationLogEntity> findByRecipientPhoneOrderBySentAtDesc(String recipientPhone);

    List<NotificationLogEntity> findAllByOrderBySentAtDesc();
}
