package com.kab.qershi.notification.application.usecase;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.NotificationRequest;
import com.kab.qershi.notification.domain.model.NotificationStatus;
import com.kab.qershi.notification.domain.model.NotificationTemplate;
import com.kab.qershi.notification.domain.model.SmsGatewayConfig;
import com.kab.qershi.notification.domain.ports.inbound.SendNotificationUseCase;
import com.kab.qershi.notification.domain.ports.outbound.NotificationProviderPort;
import com.kab.qershi.notification.domain.ports.outbound.NotificationRepositoryPort;
import com.kab.qershi.notification.domain.ports.outbound.SmsGatewayConfigRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Service implementing SendNotificationUseCase for SMS messaging dispatches.
 * Handles template rendering, tenant-specific dynamic provider routing, and audit logging.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class SmsNotificationService implements SendNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    private final NotificationRepositoryPort repositoryPort;
    private final SmsGatewayConfigRepositoryPort configRepositoryPort;
    private final NotificationProviderFactory providerFactory;

    public SmsNotificationService(NotificationRepositoryPort repositoryPort,
                                  SmsGatewayConfigRepositoryPort configRepositoryPort,
                                  NotificationProviderFactory providerFactory) {
        this.repositoryPort = repositoryPort;
        this.configRepositoryPort = configRepositoryPort;
        this.providerFactory = providerFactory;
    }

    @Override
    @Transactional
    public NotificationLog sendDirectSms(String recipientPhone, String message) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            throw new IllegalArgumentException("Recipient phone number cannot be null or blank.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message content cannot be null or blank.");
        }

        log.info("Sending direct SMS to recipient phone: {}", maskPhone(recipientPhone));

        SmsGatewayConfig activeConfig = configRepositoryPort.findActiveConfig().orElse(null);
        NotificationProviderPort provider = (activeConfig != null)
                ? providerFactory.getProvider(activeConfig.getProvider())
                : providerFactory.getProvider((String) null);

        String vendorResponse = provider.sendSms(recipientPhone, message, activeConfig);

        NotificationStatus status = (vendorResponse != null && vendorResponse.contains("ERROR"))
                ? NotificationStatus.FAILED
                : NotificationStatus.SENT;

        NotificationLog logEntry = new NotificationLog(
                null,
                recipientPhone,
                NotificationChannel.SMS,
                "DIRECT_SMS",
                message,
                status,
                vendorResponse,
                Instant.now()
        );

        return repositoryPort.saveLog(logEntry);
    }

    @Override
    @Transactional
    public NotificationLog sendTemplatedNotification(NotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("NotificationRequest cannot be null.");
        }
        if (request.getRecipientPhone() == null || request.getRecipientPhone().isBlank()) {
            throw new IllegalArgumentException("Recipient phone number cannot be null or blank.");
        }
        if (request.getTemplateCode() == null || request.getTemplateCode().isBlank()) {
            throw new IllegalArgumentException("Template code cannot be null or blank.");
        }

        log.info("Processing templated notification [Code: {}] for recipient: {}",
                request.getTemplateCode(), maskPhone(request.getRecipientPhone()));

        Optional<NotificationTemplate> templateOpt = repositoryPort.findTemplateByCode(request.getTemplateCode());
        if (templateOpt.isEmpty()) {
            log.warn("Template code '{}' not found in tenant schema. Fallback to raw message.", request.getTemplateCode());
            String rawText = request.getRawMessage() != null ? request.getRawMessage() : "Notification: " + request.getTemplateCode();
            return sendDirectSms(request.getRecipientPhone(), rawText);
        }

        NotificationTemplate template = templateOpt.get();
        if (!template.isActive()) {
            log.warn("Template code '{}' is currently deactivated for this tenant.", request.getTemplateCode());
            return new NotificationLog(
                    null,
                    request.getRecipientPhone(),
                    NotificationChannel.SMS,
                    request.getTemplateCode(),
                    "TEMPLATE_DEACTIVATED",
                    NotificationStatus.FAILED,
                    "{\"error\":\"Template deactivated\"}",
                    Instant.now()
            );
        }

        String renderedMessage = template.render(request.getParameters());

        SmsGatewayConfig activeConfig = configRepositoryPort.findActiveConfig().orElse(null);
        NotificationProviderPort provider;
        if (request.getProviderBeanName() != null && !request.getProviderBeanName().isBlank()) {
            provider = providerFactory.getProvider(request.getProviderBeanName());
        } else if (activeConfig != null) {
            provider = providerFactory.getProvider(activeConfig.getProvider());
        } else {
            provider = providerFactory.getProvider((String) null);
        }

        String vendorResponse = provider.sendSms(request.getRecipientPhone(), renderedMessage, activeConfig);

        NotificationStatus status = (vendorResponse != null && vendorResponse.contains("ERROR"))
                ? NotificationStatus.FAILED
                : NotificationStatus.SENT;

        NotificationLog logEntry = new NotificationLog(
                null,
                request.getRecipientPhone(),
                template.getChannel(),
                template.getTemplateCode(),
                renderedMessage,
                status,
                vendorResponse,
                Instant.now()
        );

        return repositoryPort.saveLog(logEntry);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 3);
    }
}
