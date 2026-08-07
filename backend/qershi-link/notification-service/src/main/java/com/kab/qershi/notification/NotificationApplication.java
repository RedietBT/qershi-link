package com.kab.qershi.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for SACCO Core Banking Notification & Messaging Service.
 * Runs on HTTP Port 8086 and gRPC Port 9086.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@SpringBootApplication
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
