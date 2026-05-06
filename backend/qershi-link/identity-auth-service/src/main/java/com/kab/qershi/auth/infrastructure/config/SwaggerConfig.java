package com.kab.qershi.auth.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Qershi-Link Identity Auth API")
                        .version("1.0")
                        .description("Authentication and Authorization service for the SACCO Digitization project.")
                        .contact(new Contact()
                                .name("KAB Digital Solution PLC")
                                .url("https://kabdigital.com")));
    }
}