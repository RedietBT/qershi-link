package com.kab.qershi.hub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Server-Side OpenAPI Proxy Controller for Centralized Swagger Hub.
 * Proxies internal microservice /v3/api-docs requests on the backend to bypass browser CORS restrictions
 * and rewrites the server URL target to http://localhost:8080 and http://localhost:8081 for direct browser execution.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.1
 */
@RestController
@RequestMapping("/api-docs")
public class ApiDocProxyController {

    private static final Logger log = LoggerFactory.getLogger(ApiDocProxyController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/identity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getIdentityApiDocs() {
        return fetchApiDocs("identity-auth-service", 8080);
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProfileApiDocs() {
        return fetchApiDocs("profile-service", 8081);
    }

    @GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAccountApiDocs() {
        return fetchApiDocs("account-management-service", 8082);
    }

    @GetMapping(value = "/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTransactionApiDocs() {
        return fetchApiDocs("transaction-management-service", 8083);
    }

    @GetMapping(value = "/notification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getNotificationApiDocs() {
        return fetchApiDocs("notification-service", 8086);
    }

    private ResponseEntity<String> fetchApiDocs(String serviceName, int port) {
        String[] candidateUrls = new String[]{
                "http://" + serviceName + ":" + (serviceName.contains("identity") ? 8080 : port) + "/v3/api-docs",
                "http://localhost:" + port + "/v3/api-docs"
        };

        for (String url : candidateUrls) {
            try {
                log.info("Fetching internal OpenAPI docs from: {}", url);
                String docs = restTemplate.getForObject(url, String.class);
                if (docs != null && docs.contains("openapi")) {
                    docs = sanitizeServers(docs, port, serviceName);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(docs);
                }
            } catch (Exception ex) {
                log.warn("Failed fetching from {}: {}", url, ex.getMessage());
            }
        }

        return ResponseEntity.status(503)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"openapi\":\"3.1.0\",\"info\":{\"title\":\"" + serviceName + " Offline\",\"version\":\"1.0.0\"},\"paths\":{}}");
    }

    private String sanitizeServers(String rawDocs, int port, String serviceName) {
        try {
            JsonNode root = objectMapper.readTree(rawDocs);
            if (root instanceof ObjectNode objectNode) {
                ArrayNode serversNode = objectMapper.createArrayNode();

                ObjectNode localhostServer = objectMapper.createObjectNode();
                localhostServer.put("url", "http://localhost:" + port);
                localhostServer.put("description", "Direct Localhost Target (Port " + port + ")");
                serversNode.add(localhostServer);

                ObjectNode k8sServer = objectMapper.createObjectNode();
                k8sServer.put("url", "http://" + serviceName + ":" + port);
                k8sServer.put("description", "Kubernetes Pod Target");
                serversNode.add(k8sServer);

                objectNode.set("servers", serversNode);
                return objectMapper.writeValueAsString(objectNode);
            }
        } catch (Exception e) {
            log.error("Failed to sanitize servers JSON for {}: {}", serviceName, e.getMessage());
        }
        return rawDocs;
    }
}
