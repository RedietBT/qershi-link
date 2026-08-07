package com.kab.qershi.notification.infrastructure.grpc;

import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLanguage;
import com.kab.qershi.notification.domain.model.NotificationLog;
import com.kab.qershi.notification.domain.model.NotificationRequest;
import com.kab.qershi.notification.domain.ports.inbound.SendNotificationUseCase;
import com.kab.qershi.notification.infrastructure.config.TenantContext;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-speed gRPC server service exposing SMS notification dispatches to inter-service clients.
 * Listens on gRPC Port 9086.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@GrpcService
public class NotificationGrpcServiceImpl extends NotificationGrpcServiceGrpc.NotificationGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcServiceImpl.class);
    private final SendNotificationUseCase sendNotificationUseCase;

    public NotificationGrpcServiceImpl(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @Override
    public void sendSmsNotification(SendSmsProtoRequest request, StreamObserver<SendSmsProtoResponse> responseObserver) {
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }

            log.info("Received gRPC SMS notification request for recipient: {}", request.getRecipientPhone());

            NotificationLog logEntry;
            if (request.getTemplateCode() != null && !request.getTemplateCode().isBlank()) {
                NotificationRequest domainRequest = new NotificationRequest(
                        request.getRecipientPhone(),
                        request.getTemplateCode(),
                        request.getRawMessage(),
                        request.getParametersMap(),
                        NotificationChannel.SMS,
                        NotificationLanguage.EN,
                        request.getProviderBeanName()
                );
                logEntry = sendNotificationUseCase.sendTemplatedNotification(domainRequest);
            } else {
                logEntry = sendNotificationUseCase.sendDirectSms(request.getRecipientPhone(), request.getRawMessage());
            }

            boolean isSuccess = logEntry.getStatus() != null && logEntry.getStatus().name().equals("SENT");

            SendSmsProtoResponse response = SendSmsProtoResponse.newBuilder()
                    .setSuccess(isSuccess)
                    .setLogId(logEntry.getLogId() != null ? logEntry.getLogId().toString() : "")
                    .setStatus(logEntry.getStatus() != null ? logEntry.getStatus().name() : "UNKNOWN")
                    .setMessage(logEntry.getRenderedMessage() != null ? logEntry.getRenderedMessage() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Error processing gRPC SMS notification request: {}", ex.getMessage(), ex);
            SendSmsProtoResponse errorResponse = SendSmsProtoResponse.newBuilder()
                    .setSuccess(false)
                    .setStatus("ERROR")
                    .setMessage(ex.getMessage() != null ? ex.getMessage() : "Internal gRPC error")
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        } finally {
            TenantContext.clear();
        }
    }
}
