package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.ports.outbound.ProfileClientPort;
import com.kab.qershi.auth.infrastructure.grpc.ProfileServiceClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Outbound adapter bridging ProfileClientPort to downstream gRPC ProfileServiceClient.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ProfileClientAdapter implements ProfileClientPort {

    private final ProfileServiceClient profileServiceClient;

    public ProfileClientAdapter(ProfileServiceClient profileServiceClient) {
        this.profileServiceClient = profileServiceClient;
    }

    @Override
    public void triggerProfileCascadeDeletion(UUID userId) {
        profileServiceClient.triggerProfileCascadeDeletion(userId);
    }
}
