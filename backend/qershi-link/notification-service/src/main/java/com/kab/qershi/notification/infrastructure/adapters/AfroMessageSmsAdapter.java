package com.kab.qershi.notification.infrastructure.adapters;

import com.kab.qershi.notification.domain.model.SmsGatewayConfig;
import com.kab.qershi.notification.domain.ports.outbound.NotificationProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Outbound SMS Messaging Adapter interfacing with AfroMessage HTTP Gateway.
 * Supports both tenant-specific dynamic API keys/Sender IDs and environment fallbacks.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component("afroMessageSmsAdapter")
public class AfroMessageSmsAdapter implements NotificationProviderPort {

    private static final Logger log = LoggerFactory.getLogger(AfroMessageSmsAdapter.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private static final Set<String> DUMMY_TEST_PATTERNS = Set.of(
            "911223344",
            "988112233",
            "987654321",
            "912345678",
            "900000000"
    );

    @Value("${afromessage.api.key:mock-key}")
    private String defaultApiKey;

    @Value("${afromessage.api.url:https://api.afromessage.com/api/send}")
    private String defaultApiUrl;

    @Override
    public String sendSms(String recipientPhone, String message, SmsGatewayConfig config) {
        log.info("Preparing to dispatch SMS notification via AfroMessage gateway");

        if (isDummyTestPhoneNumber(recipientPhone)) {
            log.warn("Blocked live SMS dispatch for dummy/test phone number: {}", recipientPhone);
            return "{\"status\":\"SIMULATED_TEST_MODE\",\"detail\":\"Blocked dummy phone number\"}";
        }

        String effectiveApiKey = (config != null && config.getApiKey() != null && !config.getApiKey().isBlank())
                ? config.getApiKey()
                : defaultApiKey;

        String effectiveApiUrl = (config != null && config.getApiUrl() != null && !config.getApiUrl().isBlank())
                ? config.getApiUrl()
                : defaultApiUrl;

        String effectiveSender = (config != null && config.getSenderId() != null)
                ? config.getSenderId()
                : "";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (effectiveApiKey != null && !effectiveApiKey.isBlank() && !effectiveApiKey.equalsIgnoreCase("mock-key")) {
            headers.setBearerAuth(effectiveApiKey);
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("to", recipientPhone);
        payload.put("message", message);
        payload.put("sender", effectiveSender);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        try {
            if (effectiveApiUrl != null && !effectiveApiUrl.isBlank() && !effectiveApiUrl.contains("example.com")) {
                ResponseEntity<String> response = restTemplate.postForEntity(effectiveApiUrl, request, String.class);
                log.info("SMS dispatched via AfroMessage gateway to {}. Response: {}", recipientPhone, response.getBody());
                return response.getBody() != null ? response.getBody() : "{\"status\":\"SUCCESS\"}";
            } else {
                log.warn("AfroMessage API URL unconfigured. SMS notification simulated.");
                return "{\"status\":\"SIMULATED_UNCONFIGURED\",\"detail\":\"AfroMessage URL missing\"}";
            }
        } catch (Exception e) {
            log.error("Failed to send SMS notification via AfroMessage to {}: {}", recipientPhone, e.getMessage());
            return "{\"status\":\"ERROR\",\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private boolean isDummyTestPhoneNumber(String recipientPhone) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            return true;
        }
        String digitsOnly = recipientPhone.replaceAll("[^0-9]", "");
        for (String pattern : DUMMY_TEST_PATTERNS) {
            if (digitsOnly.endsWith(pattern)) {
                return true;
            }
        }
        return digitsOnly.endsWith("000000");
    }
}
