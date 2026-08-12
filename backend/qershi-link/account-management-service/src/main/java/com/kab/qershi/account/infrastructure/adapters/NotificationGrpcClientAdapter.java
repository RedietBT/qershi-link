package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.infrastructure.config.TenantContext;
import com.kab.qershi.notification.infrastructure.grpc.NotificationGrpcServiceGrpc;
import com.kab.qershi.notification.infrastructure.grpc.SendSmsProtoRequest;
import com.kab.qershi.notification.infrastructure.grpc.SendSmsProtoResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Outbound gRPC client adapter for dispatching account opening SMS notifications via notification-service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class NotificationGrpcClientAdapter {

    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcClientAdapter.class);

    @GrpcClient("notification-service")
    private NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationStub;

    public void sendAccountOpenedNotification(String recipientPhone, String memberName, String accountNo, String productName, String saccoName) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            log.warn("Cannot dispatch account opened SMS: Recipient phone is null or blank.");
            return;
        }

        try {
            String tenantSchema = TenantContext.getTenantSchema();
            log.info("Sending Account Opened SMS [AccountNo: {}] via gRPC notification-service for schema: {}", accountNo, tenantSchema);

            Map<String, String> params = new HashMap<>();
            params.put("memberName", memberName != null ? memberName : "Valued Member");
            params.put("accountNo", accountNo);
            params.put("productName", productName != null ? productName : "Deposit Account");
            params.put("saccoName", saccoName != null && !saccoName.isBlank() ? saccoName : "SACCO");

            SendSmsProtoRequest request = SendSmsProtoRequest.newBuilder()
                    .setTenantSchema(tenantSchema != null ? tenantSchema : "master_schema")
                    .setRecipientPhone(recipientPhone)
                    .setTemplateCode("ACCOUNT_OPENED_ALERT")
                    .putAllParameters(params)
                    .build();

            SendSmsProtoResponse response = notificationStub.sendSmsNotification(request);
            if (response.getSuccess()) {
                log.info("Account Opened SMS successfully dispatched via gRPC. Log ID: {}", response.getLogId());
            } else {
                log.warn("gRPC notification-service returned status: {}", response.getStatus());
            }
        } catch (Exception ex) {
            log.error("Failed sending account opened SMS via gRPC: {}", ex.getMessage(), ex);
        }
    }
}
