package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${afromessage.api.key}")
    private String apiKey;

    @Value("${afromessage.api.url}")
    private String apiUrl;

    @Override
    public void sendSms(String msisdn, String message) {
        // 1. Setup Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 2. Build Payload
        Map<String, String> payload = new HashMap<>();
        payload.put("to", msisdn);
        payload.put("message", message);
        payload.put("sender", "");
        // Note: Check AfroMessage docs if you need "from" (Sender ID) here

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        // 3. Execute Request
        try {
            restTemplate.postForEntity(apiUrl, request, String.class);
        } catch (Exception e) {
            // Log the error so you know if SMS sending fails
            System.err.println("Failed to send SMS to " + msisdn + ": " + e.getMessage());
        }
    }
}