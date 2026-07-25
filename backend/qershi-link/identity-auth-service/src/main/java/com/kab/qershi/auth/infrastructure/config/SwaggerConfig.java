package com.kab.qershi.auth.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Technical Configuration Section: OpenAPI Documentation Generator.
 * Provisions automated OpenAPI specifications and Swagger UI parameters for endpoints profiling.
 * Enforces dynamic clarity across input models, custom regex validations, and onboarding API rules.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.1
 */
@Configuration
public class SwaggerConfig {

    /**
     * Defines the structural OpenAPI documentation container metadata for the identity domain service.
     *
     * @return OpenAPI The configured documentation bean wrapper.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Qershi-Link Identity Auth API")
                        .version("1.0.0")
                        .description("Authentication and Authorization service for the SACCO Digitization project.")
                        .contact(new Contact()
                                .name("KAB Digital Solution PLC")
                                .url("https://kabdigital.com")))
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