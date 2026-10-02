package com.kab.qershi.notification.application.usecase;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.NotificationStatus;
import com.kab.qershi.notification.domain.model.SmsGatewayConfig;
import com.kab.qershi.notification.domain.model.SmsProviderType;
import com.kab.qershi.notification.domain.ports.inbound.SmsGatewayConfigUseCase;
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
 * Service implementation for managing and testing SACCO SMS Gateway configurations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class SmsGatewayConfigService implements SmsGatewayConfigUseCase {

    private static final Logger log = LoggerFactory.getLogger(SmsGatewayConfigService.class);

    private final SmsGatewayConfigRepositoryPort configRepositoryPort;
    private final NotificationRepositoryPort notificationRepositoryPort;
    private final NotificationProviderFactory providerFactory;

    public SmsGatewayConfigService(SmsGatewayConfigRepositoryPort configRepositoryPort,
                                   NotificationRepositoryPort notificationRepositoryPort,
                                   NotificationProviderFactory providerFactory) {
        this.configRepositoryPort = configRepositoryPort;
        this.notificationRepositoryPort = notificationRepositoryPort;
        this.providerFactory = providerFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public SmsGatewayConfig getActiveConfig() {
        return configRepositoryPort.findActiveConfig().orElseGet(() -> {
            SmsGatewayConfig defaultConfig = new SmsGatewayConfig();
            defaultConfig.setProvider(SmsProviderType.AFROMESSAGE);
            defaultConfig.setSenderId("QERSHI");
            defaultConfig.setApiUrl("https://api.afromessage.com/api/send");
            defaultConfig.setActive(true);
            return defaultConfig;
        });
    }

    @Override
    @Transactional
    public SmsGatewayConfig saveConfig(SmsGatewayConfig newConfig) {
        if (newConfig == null) {
            throw new IllegalArgumentException("SMS Gateway configuration cannot be null.");
        }

        Optional<SmsGatewayConfig> existingOpt = configRepositoryPort.findActiveConfig();

        if (existingOpt.isPresent()) {
            SmsGatewayConfig existing = existingOpt.get();
            existing.setProvider(newConfig.getProvider());
            existing.setSenderId(newConfig.getSenderId());

            // Preserve existing API key if masked or left blank
            if (newConfig.getApiKey() != null && !newConfig.getApiKey().isBlank()
                    && !newConfig.getApiKey().contains("••••")) {
                existing.setApiKey(newConfig.getApiKey().trim());
            }

            // Preserve existing API secret if masked or left blank
            if (newConfig.getApiSecret() != null && !newConfig.getApiSecret().isBlank()
                    && !newConfig.getApiSecret().contains("••••")) {
                existing.setApiSecret(newConfig.getApiSecret().trim());
            }

            existing.setApiUrl(newConfig.getApiUrl());
            existing.setServiceAccountId(newConfig.getServiceAccountId());
            existing.setExtraHeadersJson(newConfig.getExtraHeadersJson());
            existing.setActive(newConfig.isActive());
            existing.setUpdatedAt(Instant.now());

            log.info("Updating existing active SMS gateway config for tenant [Provider: {}]", existing.getProvider());
            return configRepositoryPort.save(existing);
        } else {
            newConfig.setCreatedAt(Instant.now());
            newConfig.setUpdatedAt(Instant.now());
            log.info("Creating new active SMS gateway config for tenant [Provider: {}]", newConfig.getProvider());
            return configRepositoryPort.save(newConfig);
        }
    }

    @Override
    @Transactional
    public NotificationLog testSmsGateway(String testPhone, String customMessage) {
        if (testPhone == null || testPhone.isBlank()) {
            throw new IllegalArgumentException("Test recipient phone number cannot be blank.");
        }

        SmsGatewayConfig activeConfig = getActiveConfig();
        String message = (customMessage != null && !customMessage.isBlank())
                ? customMessage
                : "Qershi-Link Test: SMS Gateway connection verified successfully for " + activeConfig.getProvider() + ".";

        log.info("Dispatching test SMS to {} using provider: {}", testPhone, activeConfig.getProvider());

        NotificationProviderPort provider = providerFactory.getProvider(activeConfig.getProvider());
        String vendorResponse = provider.sendSms(testPhone, message, activeConfig);

        NotificationStatus status = (vendorResponse != null && vendorResponse.contains("ERROR"))
                ? NotificationStatus.FAILED
                : NotificationStatus.SENT;

        NotificationLog logEntry = new NotificationLog(
                null,
                testPhone,
                NotificationChannel.SMS,
                "TEST_GATEWAY_PING",
                message,
                status,
                vendorResponse,
                Instant.now()
        );

        return notificationRepositoryPort.saveLog(logEntry);
    }
}
