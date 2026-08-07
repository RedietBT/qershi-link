package com.kab.qershi.loan.management.infrastructure.adapters;

import com.kab.qershi.loan.management.infrastructure.config.TenantContext;
import com.kab.qershi.notification.infrastructure.grpc.NotificationGrpcServiceGrpc;
import com.kab.qershi.notification.infrastructure.grpc.SendSmsProtoRequest;
import com.kab.qershi.notification.infrastructure.grpc.SendSmsProtoResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Outbound gRPC client adapter calling notification-service on gRPC port 9086.
 * Dispatches automated loan management SMS alerts (e.g. LOAN_DISBURSED, LOAN_REPAYMENT_CONFIRMATION).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class NotificationGrpcClientAdapter {

    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcClientAdapter.class);

    @GrpcClient("notification-service")
    private NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationStub;

    public void sendNotification(String recipientPhone, String templateCode, Map<String, String> parameters) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            log.warn("Cannot dispatch loan management SMS: Recipient phone is null or blank.");
            return;
        }

        try {
            String tenantSchema = TenantContext.getTenantSchema();
            log.info("Sending loan management SMS [Template: {}] to {} via gRPC notification-service for schema: {}",
                    templateCode, recipientPhone, tenantSchema);

            SendSmsProtoRequest request = SendSmsProtoRequest.newBuilder()
                    .setTenantSchema(tenantSchema != null ? tenantSchema : "master_schema")
                    .setRecipientPhone(recipientPhone)
                    .setTemplateCode(templateCode)
                    .putAllParameters(parameters != null ? parameters : Map.of())
                    .build();

            SendSmsProtoResponse response = notificationStub.sendSmsNotification(request);
            log.info("gRPC Notification Service Response -> Success: {}, Log ID: {}, Status: {}",
                    response.getSuccess(), response.getLogId(), response.getStatus());
        } catch (Exception ex) {
            log.error("Failed to send loan management SMS via gRPC: {}", ex.getMessage(), ex);
        }
    }
}
