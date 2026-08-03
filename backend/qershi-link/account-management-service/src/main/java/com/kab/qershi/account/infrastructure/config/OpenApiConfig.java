package com.kab.qershi.account.infrastructure.config;

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
 * OpenAPI / Swagger UI Interactive Documentation Configuration for Qershi Link Account Management Service.
 * Configured with JWT Bearer Authentication Security Scheme for interactive API authorization testing.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Qershi Link - Account Management Service API")
                        .description("Core Microservice API managing SACCO dynamic deposit product definitions, member savings account opening with ISO Luhn account number generation, Four-Eye Maker-Checker approvals, monetary lien balance holds, and administrative freezes.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KAB Digital Solution PLC")
                                .email("support@kabdigital.com"))
                        .license(new License()
                                .name("Proprietary License")
                                .url("https://kabdigital.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local Development Server"),
                        new Server().url("http://account-management-service:8082").description("Kubernetes Service Internal"),
                        new Server().url("https://api.qershilink.com/account").description("Production Environment")
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
