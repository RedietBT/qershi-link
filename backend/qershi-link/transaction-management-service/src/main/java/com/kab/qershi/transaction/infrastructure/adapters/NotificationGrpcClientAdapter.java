package com.kab.qershi.transaction.infrastructure.adapters;

import com.kab.qershi.notification.infrastructure.grpc.NotificationGrpcServiceGrpc;
import com.kab.qershi.notification.infrastructure.grpc.SendSmsProtoRequest;
import com.kab.qershi.notification.infrastructure.grpc.SendSmsProtoResponse;
import com.kab.qershi.transaction.infrastructure.config.TenantContext;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Outbound gRPC client adapter for dispatching transaction SMS notifications via notification-service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class NotificationGrpcClientAdapter {

    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcClientAdapter.class);

    @GrpcClient("notification-service")
    private NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationStub;

    public void sendCashDepositNotification(String recipientPhone, String memberName, String accountNo, BigDecimal amount, BigDecimal newBalance) {
        Map<String, String> params = new HashMap<>();
        params.put("memberName", memberName != null ? memberName : "Valued Member");
        params.put("accountNo", accountNo);
        params.put("amount", amount != null ? amount.stripTrailingZeros().toPlainString() : "0");
        params.put("balance", newBalance != null ? newBalance.stripTrailingZeros().toPlainString() : "0");

        dispatchNotification(recipientPhone, "CASH_DEPOSIT_ALERT", params);
    }

    public void sendCashWithdrawalNotification(String recipientPhone, String memberName, String accountNo, BigDecimal amount, BigDecimal newBalance) {
        Map<String, String> params = new HashMap<>();
        params.put("memberName", memberName != null ? memberName : "Valued Member");
        params.put("accountNo", accountNo);
        params.put("amount", amount != null ? amount.stripTrailingZeros().toPlainString() : "0");
        params.put("balance", newBalance != null ? newBalance.stripTrailingZeros().toPlainString() : "0");

        dispatchNotification(recipientPhone, "CASH_WITHDRAWAL_ALERT", params);
    }

    public void sendTransferNotification(String recipientPhone, String memberName, String receiverName, String receiverAccountNo, BigDecimal amount, BigDecimal newBalance) {
        Map<String, String> params = new HashMap<>();
        params.put("memberName", memberName != null ? memberName : "Valued Member");
        params.put("receiverName", receiverName != null ? receiverName : "Recipient");
        params.put("receiverAccountNo", receiverAccountNo);
        params.put("amount", amount != null ? amount.stripTrailingZeros().toPlainString() : "0");
        params.put("balance", newBalance != null ? newBalance.stripTrailingZeros().toPlainString() : "0");

        dispatchNotification(recipientPhone, "TRANSFER_SENT_ALERT", params);
    }

    private void dispatchNotification(String recipientPhone, String templateCode, Map<String, String> parameters) {
        String targetPhone = (recipientPhone != null && !recipientPhone.isBlank()) ? recipientPhone : "+251911223344";

        try {
            String tenantSchema = TenantContext.getTenantSchema();
            log.info("Sending transaction SMS [Template: {}] to {} via gRPC notification-service for schema: {}", templateCode, targetPhone, tenantSchema);

            SendSmsProtoRequest request = SendSmsProtoRequest.newBuilder()
                    .setTenantSchema(tenantSchema != null ? tenantSchema : "master_schema")
                    .setRecipientPhone(targetPhone)
                    .setTemplateCode(templateCode)
                    .putAllParameters(parameters)
                    .build();

            SendSmsProtoResponse response = notificationStub.sendSmsNotification(request);
            if (response.getSuccess()) {
                log.info("Transaction SMS successfully dispatched via gRPC. Log ID: {}", response.getLogId());
            } else {
                log.warn("gRPC notification-service returned status: {}", response.getStatus());
            }
        } catch (Exception ex) {
            log.error("Failed sending transaction SMS via gRPC: {}", ex.getMessage(), ex);
        }
    }
}
