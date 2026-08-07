package com.kab.qershi.loan.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for Loan Management Service (LMS).
 * Manages post-origination loan accounts, repayment schedule calculation,
 * payment waterfall, delinquency tracking, and inter-service gRPC integrations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@SpringBootApplication
public class LoanManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanManagementApplication.class, args);
    }
}
