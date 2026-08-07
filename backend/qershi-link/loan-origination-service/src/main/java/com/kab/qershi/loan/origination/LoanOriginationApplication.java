package com.kab.qershi.loan.origination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot main entrypoint for loan-origination-service.
 * Serves as the gatekeeper of SACCO credit origination, borrowing group management,
 * pre-eligibility multi-factor scoring, and Maker-Checker approval workflows.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@SpringBootApplication
public class LoanOriginationApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanOriginationApplication.class, args);
    }
}
