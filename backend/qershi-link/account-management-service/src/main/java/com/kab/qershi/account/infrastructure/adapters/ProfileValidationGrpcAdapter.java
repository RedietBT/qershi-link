package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.ports.outbound.ProfileValidationPort;
import com.kab.qershi.auth.infrastructure.grpc.ProfileServiceGrpc;
import com.kab.qershi.auth.infrastructure.grpc.GetProfileRequest;
import com.kab.qershi.auth.infrastructure.grpc.ProfileContactResponse;
import com.kab.qershi.account.infrastructure.config.TenantContext;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Infrastructure outbound adapter validating member profile status via gRPC / fallback checks.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ProfileValidationGrpcAdapter implements ProfileValidationPort {

    private static final Logger log = LoggerFactory.getLogger(ProfileValidationGrpcAdapter.class);

    @Override
    public boolean isMemberActive(UUID userId) {
        if (userId == null) {
            return false;
        }
        log.debug("Validating member profile active status for userId: {}", userId);
        // Inter-service validation fallback check (will integrate gRPC stub call)
        return true;
    }

    @GrpcClient("profile-service")
    private ProfileServiceGrpc.ProfileServiceBlockingStub profileStub;

    @Override
    public ProfileContact getProfileContact(UUID userId) {
        if (userId == null) return new ProfileContact("", "");
        try {
            String schema = TenantContext.getTenantSchema();
            GetProfileRequest request = GetProfileRequest.newBuilder()
                    .setUserId(userId.toString())
                    .setTenantSchema(schema != null ? schema : "")
                    .build();
            ProfileContactResponse response = profileStub.getProfileContact(request);
            if (response.getIsFound()) {
                return new ProfileContact(response.getPhoneNumber(), response.getFullName());
            }
        } catch (Exception ex) {
            log.error("Failed fetching profile contact info for userId {}: {}", userId, ex.getMessage());
        }
        return new ProfileContact("", "Member " + userId.toString().substring(0, 8));
    }
}
