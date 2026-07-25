package com.kab.qershi.auth.infrastructure.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Outbound gRPC Client orchestration adapter triggering data cascade routines
 * across independent microservice domains.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ProfileServiceClient {

    // 🛠️ FIXED: Changed outer class reference to ProfileServiceGrpcGrpc to match the protobuf compiler output
    @GrpcClient("profile-service")
    private ProfileServiceGrpcGrpc.ProfileServiceGrpcBlockingStub profileServiceStub;

    /**
     * Dispatches a synchronous procedure call to drop matching records from profile_schema.profile.
     *
     * @param userId The unique identity handle of the purged user account.
     * @return boolean True if the profile service executed the wipe successfully.
     */
    public boolean triggerProfileCascadeDeletion(UUID userId) {
        ResourceDeleteRequest request = ResourceDeleteRequest.newBuilder()
                .setUserId(userId.toString())
                .build();
        try {
            // ✅ This method will now resolve cleanly once the parent stub class is found
            ResourceDeleteResponse response = profileServiceStub.cascadeDeleteProfile(request);
            return response.getIsSuccess();
        } catch (Exception ex) {
            // Log environmental transport failures safely here
            return false;
        }
    }
}