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
 * Outbound SMS Messaging Adapter interfacing with Ethio Telecom Enterprise Bulk SMS Gateway.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component("ethioTelecomSmsAdapter")
public class EthioTelecomSmsAdapter implements NotificationProviderPort {

    private static final Logger log = LoggerFactory.getLogger(EthioTelecomSmsAdapter.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String DEFAULT_ETHIO_TELECOM_URL = "https://bulksms.ethiotelecom.et/api/v1/sms/send";

    @Override
    public String sendSms(String recipientPhone, String message, SmsGatewayConfig config) {
        log.info("Preparing to dispatch SMS notification via Ethio Telecom Bulk Gateway");

        String apiUrl = (config != null && config.getApiUrl() != null && !config.getApiUrl().isBlank())
                ? config.getApiUrl()
                : DEFAULT_ETHIO_TELECOM_URL;

        String apiKey = (config != null) ? config.getApiKey() : null;
        String apiSecret = (config != null) ? config.getApiSecret() : null;
        String senderId = (config != null && config.getSenderId() != null) ? config.getSenderId() : "ETHIOTEL";
        String serviceAccountId = (config != null) ? config.getServiceAccountId() : null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            if (apiSecret != null && !apiSecret.isBlank()) {
                headers.setBasicAuth(apiKey, apiSecret);
            } else {
                headers.setBearerAuth(apiKey);
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("recipient", recipientPhone);
        payload.put("message", message);
        payload.put("sender_id", senderId);
        if (serviceAccountId != null && !serviceAccountId.isBlank()) {
            payload.put("service_account_id", serviceAccountId);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            if (apiUrl.contains("mock") || apiUrl.contains("localhost") || apiUrl.contains("example.com")) {
                log.info("Ethio Telecom URL in test/simulated mode. Payload prepared: {}", payload);
                return "{\"status\":\"SUCCESS\",\"provider\":\"ETHIO_TELECOM\",\"detail\":\"Simulated gateway response\"}";
            }

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            log.info("SMS dispatched via Ethio Telecom to {}. Response: {}", recipientPhone, response.getBody());
            return response.getBody() != null ? response.getBody() : "{\"status\":\"SUCCESS\"}";
        } catch (Exception e) {
            log.error("Failed to send SMS via Ethio Telecom to {}: {}", recipientPhone, e.getMessage());
            return "{\"status\":\"ERROR\",\"provider\":\"ETHIO_TELECOM\",\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
