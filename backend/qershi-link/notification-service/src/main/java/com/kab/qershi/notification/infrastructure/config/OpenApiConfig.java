package com.kab.qershi.notification.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for Notification & Messaging Service.
 * Adds Bearer JWT Authorization locks to Swagger UI.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Qershi Link - Notification & Messaging Service API")
                        .description("Centralized SMS/Email/Push Messaging Engine with AfroMessage Integration, Multi-Language Templates, and Audit Trail Logs.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KAB Digital Solution PLC")
                                .email("info@kabdigital.com"))
                        .license(new License()
                                .name("Proprietary License")
                                .url("https://kabdigital.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8086").description("Local Development Server"),
                        new Server().url("http://notification-service:8086").description("Kubernetes Service Internal"),
                        new Server().url("https://api.qershilink.com/notification").description("Production Environment")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
