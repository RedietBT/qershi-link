package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.infrastructure.config.TenantContext;
import com.kab.qershi.notification.infrastructure.grpc.NotificationGrpcServiceGrpc;
import com.kab.qershi.notification.infrastructure.grpc.SendSmsProtoRequest;
import com.kab.qershi.notification.infrastructure.grpc.SendSmsProtoResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Outbound gRPC adapter implementing MessagingPort.
 * Forwards SMS dispatch requests to notification-service on gRPC port 9086.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
@Primary
public class NotificationGrpcClientAdapter implements MessagingPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcClientAdapter.class);

    @GrpcClient("notification-service")
    private NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationStub;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("NotificationGrpcClientAdapter initialized as PRIMARY MessagingPort bean!");
    }

    @Override
    public void sendSms(String msisdn, String message) {
        if (msisdn == null || msisdn.isBlank()) {
            log.warn("Cannot send SMS: Recipient MSISDN is null or blank.");
            return;
        }

        try {
            String tenantSchema = TenantContext.getTenantSchema();
            log.info("Dispatching SMS via gRPC to notification-service for schema: {}", tenantSchema);

            SendSmsProtoRequest request = SendSmsProtoRequest.newBuilder()
                    .setTenantSchema(tenantSchema != null ? tenantSchema : "master_schema")
                    .setRecipientPhone(msisdn)
                    .setRawMessage(message != null ? message : "")
                    .setTemplateCode("OTP_CODE")
                    .putParameters("otpCode", extractOtpCode(message))
                    .build();

            SendSmsProtoResponse response = notificationStub.sendSmsNotification(request);
            if (response.getSuccess()) {
                log.info("Successfully dispatched SMS via gRPC notification-service. Log ID: {}", response.getLogId());
            } else {
                log.warn("gRPC notification-service returned status: {}", response.getStatus());
            }
        } catch (Exception ex) {
            log.error("Failed sending SMS via gRPC notification-service: {}", ex.getMessage(), ex);
        }
    }

    private String extractOtpCode(String message) {
        if (message == null) return "";
        String digits = message.replaceAll("[^0-9]", "");
        return digits.length() >= 4 ? digits : message;
    }
}
