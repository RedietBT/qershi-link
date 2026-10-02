package com.kab.qershi.notification.infrastructure.adapters;

import com.kab.qershi.notification.domain.model.SmsGatewayConfig;
import com.kab.qershi.notification.domain.ports.outbound.NotificationProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Outbound SMS Messaging Adapter interfacing with generic/custom HTTP Webhooks.
 * Allows SACCOs to connect proprietary SMS gateways or third-party webhooks.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component("customWebhookSmsAdapter")
public class CustomWebhookSmsAdapter implements NotificationProviderPort {

    private static final Logger log = LoggerFactory.getLogger(CustomWebhookSmsAdapter.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String sendSms(String recipientPhone, String message, SmsGatewayConfig config) {
        log.info("Preparing to dispatch SMS notification via Custom Webhook");

        if (config == null || config.getApiUrl() == null || config.getApiUrl().isBlank()) {
            log.warn("Custom Webhook URL missing. Falling back to simulation.");
            return "{\"status\":\"SIMULATED\",\"detail\":\"No webhook URL provided\"}";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            headers.set("Authorization", config.getApiKey().startsWith("Bearer ") ? config.getApiKey() : "Bearer " + config.getApiKey());
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("to", recipientPhone);
        payload.put("message", message);
        payload.put("sender", config.getSenderId() != null ? config.getSenderId() : "QERSHI");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(config.getApiUrl(), request, String.class);
            log.info("SMS dispatched via Custom Webhook to {}. Status: {}", recipientPhone, response.getStatusCode());
            return response.getBody() != null ? response.getBody() : "{\"status\":\"SUCCESS\"}";
        } catch (Exception e) {
            log.error("Failed to dispatch via Custom Webhook: {}", e.getMessage());
            return "{\"status\":\"ERROR\",\"provider\":\"CUSTOM_WEBHOOK\",\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
