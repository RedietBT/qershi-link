package com.kab.qershi.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Main Entry Point for Qershi Link Centralized Swagger API Hub Aggregator.
 * Runs on Port 9020 providing a unified interface to select and explore interactive OpenAPI documentation across all microservices.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class SwaggerApiHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwaggerApiHubApplication.class, args);
    }
}
