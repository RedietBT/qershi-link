package com.kab.qershi.profile.infrastructure.grpc;

import com.kab.qershi.auth.infrastructure.grpc.ProfileServiceGrpc;
import com.kab.qershi.auth.infrastructure.grpc.ResourceDeleteRequest;
import com.kab.qershi.auth.infrastructure.grpc.ResourceDeleteResponse;
import com.kab.qershi.profile.domain.ports.inbound.ProfileManagementUseCase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * gRPC Server Service Handler exposing inter-service RPC endpoints for profile-service.
 * Listens on gRPC port (9081) for cascade deletion calls originating from identity-auth-service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@GrpcService
public class ProfileGrpcService extends ProfileServiceGrpc.ProfileServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ProfileGrpcService.class);

    private final ProfileManagementUseCase profileManagementUseCase;

    public ProfileGrpcService(ProfileManagementUseCase profileManagementUseCase) {
        this.profileManagementUseCase = profileManagementUseCase;
    }

    @Override
    public void cascadeDeleteProfile(ResourceDeleteRequest request,
                                     StreamObserver<ResourceDeleteResponse> responseObserver) {
        String userIdStr = request.getUserId();
        log.warn("Received gRPC cascadeDeleteProfile request for user ID: {}", userIdStr);

        try {
            UUID userId = UUID.fromString(userIdStr);
            profileManagementUseCase.deleteProfileByUserId(userId);

            ResourceDeleteResponse response = ResourceDeleteResponse.newBuilder()
                    .setIsSuccess(true)
                    .setFeedbackMessage("Member profile and associated records purged successfully.")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC cascadeDeleteProfile completed successfully for user ID: {}", userIdStr);
        } catch (Exception ex) {
            log.error("Failed to execute gRPC cascadeDeleteProfile for user ID: {}", userIdStr, ex);
            ResourceDeleteResponse response = ResourceDeleteResponse.newBuilder()
                    .setIsSuccess(false)
                    .setFeedbackMessage("Error purging profile: " + ex.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
