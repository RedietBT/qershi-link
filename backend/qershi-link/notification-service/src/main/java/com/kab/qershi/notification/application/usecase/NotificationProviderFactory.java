package com.kab.qershi.notification.application.usecase;

import com.kab.qershi.notification.domain.ports.outbound.NotificationProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for resolving active SMS Provider Outbound Adapters per tenant.
 * Defaults to AfroMessage provider adapter.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class NotificationProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(NotificationProviderFactory.class);
    private final Map<String, NotificationProviderPort> providerAdapters;

    @org.springframework.beans.factory.annotation.Value("${notification.provider.default:afroMessageSmsAdapter}")
    private String defaultProviderBeanName;

    public NotificationProviderFactory(Map<String, NotificationProviderPort> providerAdapters) {
        this.providerAdapters = providerAdapters;
    }

    /**
     * Resolves the active provider implementation bean dynamically.
     * Checks providerBeanName -> defaultProviderBeanName from environment/properties -> first registered provider.
     */
    public NotificationProviderPort getProvider(String providerBeanName) {
        if (providerBeanName != null && providerAdapters.containsKey(providerBeanName)) {
            log.debug("Resolved explicit custom SMS provider adapter: {}", providerBeanName);
            return providerAdapters.get(providerBeanName);
        }
        if (defaultProviderBeanName != null && providerAdapters.containsKey(defaultProviderBeanName)) {
            log.debug("Resolved default configured SMS provider adapter: {}", defaultProviderBeanName);
            return providerAdapters.get(defaultProviderBeanName);
        }
        // Fallback to first available provider in Spring context
        return providerAdapters.values().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No NotificationProviderPort adapter registered in Spring context."));
    }
}
