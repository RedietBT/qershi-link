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

@Component
public class AfroMessageAdapter implements MessagingPort {

    private static final Logger log = LoggerFactory.getLogger(AfroMessageAdapter.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${afromessage.api.key}")
    private String apiKey;

    @Value("${afromessage.api.url}")
    private String apiUrl;

    @Override
    public void sendSms(String msisdn, String message) {
        log.info("Preparing to send SMS notification to MSISDN [{}]: {}", msisdn, message);

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
                log.info("SMS notification successfully dispatched via AfroMessage gateway to {}", msisdn);
            } else {
                log.warn("AfroMessage API URL is unconfigured/placeholder ({}). SMS logged locally: [{}] -> {}", apiUrl, msisdn, message);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", msisdn, e.getMessage());
        }
    }
}