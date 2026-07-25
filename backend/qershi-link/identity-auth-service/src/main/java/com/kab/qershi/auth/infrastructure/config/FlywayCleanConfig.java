package com.kab.qershi.auth.infrastructure.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayCleanConfig {

    @Bean
    public FlywayMigrationStrategy cleanMigrationStrategy() {
        return flyway -> {
            // Forcefully wipes out the schema tracking memory and drops ghost tables
            flyway.clean();
            // Executes your pristine V1 script completely fresh on the clean schema
            flyway.migrate();
        };
    }
}