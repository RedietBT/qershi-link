package com.kab.qershi.pricing.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Swagger 3.0 Documentation Configuration for pricing-fee-service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pricingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Qershi-Link Core Banking - Enterprise Pricing & Tariff Service API")
                        .description("Centralized microservice managing transaction tariffs, commissions, and statutory 5% Withholding Tax.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KAB Digital Solution PLC")
                                .email("support@kabdigitalsolutions.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
