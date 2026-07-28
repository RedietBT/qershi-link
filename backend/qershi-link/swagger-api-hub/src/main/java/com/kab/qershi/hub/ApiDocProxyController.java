package com.kab.qershi.hub;

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
 * Proxies internal microservice /v3/api-docs requests on the backend to bypass browser CORS restrictions completely.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api-docs")
public class ApiDocProxyController {

    private static final Logger log = LoggerFactory.getLogger(ApiDocProxyController.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/identity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getIdentityApiDocs() {
        return fetchApiDocs("identity-auth-service", 8080);
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProfileApiDocs() {
        return fetchApiDocs("profile-service", 8081);
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
}
