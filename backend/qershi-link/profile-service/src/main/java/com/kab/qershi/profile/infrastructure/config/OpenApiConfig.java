package com.kab.qershi.profile.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI Interactive Documentation Configuration for Qershi Link Profile Service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI profileServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Qershi Link - Profile Service API")
                        .description("Core Microservice API managing SACCO member profiles, contact handles, employment records, government ID KYC verifications, and nominated beneficiaries (Next of Kin).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KAB Digital Solution PLC")
                                .email("support@kabdigital.com"))
                        .license(new License()
                                .name("Proprietary License")
                                .url("https://kabdigital.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Development Server"),
                        new Server().url("https://api.qershilink.com/profile").description("Production Environment")
                ));
    }
}
