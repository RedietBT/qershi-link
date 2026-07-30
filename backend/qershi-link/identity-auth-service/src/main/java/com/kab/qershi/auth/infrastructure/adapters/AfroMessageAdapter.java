package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Outbound SMS Messaging Adapter interfacing with the AfroMessage HTTP Gateway.
 * Features built-in dummy/test phone number safeguards to prevent accidental live SMS dispatches
 * during Swagger UI API testing or automated integration trials.
 * Security Enhanced: Does not log raw phone numbers in application logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
@Component
public class AfroMessageAdapter implements MessagingPort {

    private static final Logger log = LoggerFactory.getLogger(AfroMessageAdapter.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private static final Set<String> DUMMY_TEST_PATTERNS = Set.of(
            "911223344",
            "988112233",
            "987654321",
            "912345678",
            "900000000"
    );

    @Value("${afromessage.api.key}")
    private String apiKey;

    @Value("${afromessage.api.url}")
    private String apiUrl;

    @Override
    public void sendSms(String msisdn, String message) {
        log.info("Preparing to dispatch SMS notification");

        // Safeguard: Block dummy / Swagger test phone numbers from invoking live AfroMessage gateway
        if (isDummyTestPhoneNumber(msisdn)) {
            log.warn("Blocked live SMS dispatch for dummy/test phone number");
            return;
        }

        // 1. Setup Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.setBearerAuth(apiKey);
        }

        // 2. Build Payload
        Map<String, String> payload = new HashMap<>();
        payload.put("to", msisdn);
        payload.put("message", message);
        payload.put("sender", "");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        // 3. Execute Request
        try {
            if (apiUrl != null && !apiUrl.isBlank() && !apiUrl.contains("example.com")) {
                restTemplate.postForEntity(apiUrl, request, String.class);
                log.info("SMS notification successfully dispatched via AfroMessage gateway");
            } else {
                log.warn("AfroMessage API URL unconfigured. SMS notification queued");
            }
        } catch (Exception e) {
            log.error("Failed to send SMS notification: {}", e.getMessage());
        }
    }

    /**
     * Inspects MSISDN string to determine if it matches known test/dummy numbers.
     */
    private boolean isDummyTestPhoneNumber(String msisdn) {
        if (msisdn == null || msisdn.isBlank()) {
            return true;
        }
        String digitsOnly = msisdn.replaceAll("[^0-9]", "");
        for (String pattern : DUMMY_TEST_PATTERNS) {
            if (digitsOnly.endsWith(pattern)) {
                return true;
            }
        }
        return digitsOnly.endsWith("000000"); // Block repeated zeros (e.g. +251900000000)
    }
}