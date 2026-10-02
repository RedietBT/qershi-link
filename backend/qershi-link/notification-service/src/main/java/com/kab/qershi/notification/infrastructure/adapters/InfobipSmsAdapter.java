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
import java.util.List;
import java.util.Map;

/**
 * Outbound SMS Messaging Adapter interfacing with Infobip Enterprise API.
 * Standard endpoint: https://api.infobip.com/sms/2/text/advanced
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component("infobipSmsAdapter")
public class InfobipSmsAdapter implements NotificationProviderPort {

    private static final Logger log = LoggerFactory.getLogger(InfobipSmsAdapter.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String DEFAULT_INFOBIP_URL = "https://api.infobip.com/sms/2/text/advanced";

    @Override
    public String sendSms(String recipientPhone, String message, SmsGatewayConfig config) {
        log.info("Preparing to dispatch SMS notification via Infobip gateway");

        String apiUrl = (config != null && config.getApiUrl() != null && !config.getApiUrl().isBlank())
                ? config.getApiUrl()
                : DEFAULT_INFOBIP_URL;

        String apiKey = (config != null) ? config.getApiKey() : null;
        String senderId = (config != null && config.getSenderId() != null) ? config.getSenderId() : "InfoSMS";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("Authorization", "App " + apiKey);
        }

        Map<String, Object> destination = Map.of("to", recipientPhone);
        Map<String, Object> messageObj = new HashMap<>();
        messageObj.put("from", senderId);
        messageObj.put("destinations", List.of(destination));
        messageObj.put("text", message);

        Map<String, Object> payload = Map.of("messages", List.of(messageObj));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            if (apiUrl.contains("mock") || apiUrl.contains("localhost") || apiUrl.contains("example.com")) {
                log.info("Infobip URL in test/simulated mode. Payload: {}", payload);
                return "{\"status\":\"SUCCESS\",\"provider\":\"INFOBIP\",\"detail\":\"Simulated gateway response\"}";
            }

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            log.info("SMS dispatched via Infobip to {}. Response: {}", recipientPhone, response.getBody());
            return response.getBody() != null ? response.getBody() : "{\"status\":\"SUCCESS\"}";
        } catch (Exception e) {
            log.error("Failed to send SMS via Infobip to {}: {}", recipientPhone, e.getMessage());
            return "{\"status\":\"ERROR\",\"provider\":\"INFOBIP\",\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
