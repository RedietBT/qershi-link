package com.kab.qershi.auth.domain.ports.inbound;

import java.util.List;
import java.util.UUID;

public interface AuthenticationUseCase {

    record LoginCommand(
            String msisdn,
            String pin
    ) {}

    record UserContext(
            UUID userId,
            UUID saccoId,
            String schemaName,
            String globalRole,
            List<String> permissions
    ) {}

    record LoginResult(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserContext userContext
    ) {}

    LoginResult login(LoginCommand command);
}