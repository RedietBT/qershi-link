package com.kab.qershi.loan.management.domain.port.out;

import java.util.Map;

/**
 * Outbound Port for dispatching loan management notifications (e.g. SMS, Push).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface NotificationClientPort {

    void sendNotification(String recipientPhone, String templateCode, Map<String, String> parameters);
}
