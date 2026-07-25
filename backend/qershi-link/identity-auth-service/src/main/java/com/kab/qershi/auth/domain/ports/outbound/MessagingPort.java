package com.kab.qershi.auth.domain.ports.outbound;

public interface MessagingPort {
    void sendSms(String msisdn, String message);
}