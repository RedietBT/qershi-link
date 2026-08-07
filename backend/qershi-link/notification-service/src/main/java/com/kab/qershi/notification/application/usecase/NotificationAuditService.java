package com.kab.qershi.notification.application.usecase;

import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.ports.inbound.NotificationAuditUseCase;
import com.kab.qershi.notification.domain.ports.outbound.NotificationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementing NotificationAuditUseCase for retrieving notification delivery logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class NotificationAuditService implements NotificationAuditUseCase {

    private final NotificationRepositoryPort repositoryPort;

    public NotificationAuditService(NotificationRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLog> getNotificationLogs() {
        return repositoryPort.findAllLogs();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLog> getLogsByRecipient(String recipientPhone) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            throw new IllegalArgumentException("Recipient phone number cannot be null or blank.");
        }
        return repositoryPort.findLogsByRecipientPhone(recipientPhone.trim());
    }
}
