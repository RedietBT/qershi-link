package com.kab.qershi.transaction.domain.ports.outbound;

import java.math.BigDecimal;

/**
 * Outbound port for dispatching transactional SMS and push notifications
 * to members via the notification service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface NotificationClientPort {

    void sendCashDepositNotification(String recipientPhone, String memberName, String accountNo,
                                     BigDecimal amount, BigDecimal newBalance);

    void sendCashWithdrawalNotification(String recipientPhone, String memberName, String accountNo,
                                        BigDecimal amount, BigDecimal newBalance);

    void sendTransferNotification(String recipientPhone, String memberName, String receiverName,
                                  String receiverAccountNo, BigDecimal amount, BigDecimal newBalance);
}
