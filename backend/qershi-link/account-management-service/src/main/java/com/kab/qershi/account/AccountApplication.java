package com.kab.qershi.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot Main Entry Point for SACCO Core Banking Account Management Service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@SpringBootApplication
@EnableTransactionManagement
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
