package com.kab.qershi.notification.application.usecase;

import com.kab.qershi.notification.domain.model.SmsProviderType;
import com.kab.qershi.notification.domain.ports.outbound.NotificationProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for resolving active SMS Provider Outbound Adapters per tenant.
 * Supports resolution by provider bean name, provider type, or defaults to AfroMessage.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class NotificationProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(NotificationProviderFactory.class);
    private final Map<String, NotificationProviderPort> providerAdapters;

    @Value("${notification.provider.default:afroMessageSmsAdapter}")
    private String defaultProviderBeanName;

    public NotificationProviderFactory(Map<String, NotificationProviderPort> providerAdapters) {
        this.providerAdapters = providerAdapters;
    }

    /**
     * Resolves provider by explicit bean name.
     */
    public NotificationProviderPort getProvider(String providerBeanName) {
        if (providerBeanName != null && providerAdapters.containsKey(providerBeanName)) {
            log.debug("Resolved SMS provider adapter by bean name: {}", providerBeanName);
            return providerAdapters.get(providerBeanName);
        }
        if (defaultProviderBeanName != null && providerAdapters.containsKey(defaultProviderBeanName)) {
            log.debug("Resolved default configured SMS provider adapter: {}", defaultProviderBeanName);
            return providerAdapters.get(defaultProviderBeanName);
        }
        return providerAdapters.values().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No NotificationProviderPort adapter registered in Spring context."));
    }

    /**
     * Resolves provider by SmsProviderType enum.
     */
    public NotificationProviderPort getProvider(SmsProviderType providerType) {
        if (providerType == null) {
            return getProvider((String) null);
        }

        String beanName = switch (providerType) {
            case AFROMESSAGE -> "afroMessageSmsAdapter";
            case ETHIO_TELECOM -> "ethioTelecomSmsAdapter";
            case INFOBIP -> "infobipSmsAdapter";
            case CUSTOM_WEBHOOK -> "customWebhookSmsAdapter";
            case SIMULATED -> "simulatedSmsAdapter";
        };

        if (providerAdapters.containsKey(beanName)) {
            return providerAdapters.get(beanName);
        }

        log.warn("Adapter bean '{}' for provider type {} not found. Falling back to default.", beanName, providerType);
        return getProvider((String) null);
    }
}
