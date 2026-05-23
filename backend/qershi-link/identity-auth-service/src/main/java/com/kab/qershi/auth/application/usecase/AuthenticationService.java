package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.ports.inbound.AuthenticationUseCase;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;

import java.util.Collections;
import java.util.List;

/**
 * Service implementation handling multi-tenant user authentication checks.
 * Verifies global identities and maps local tenant schema routing keys.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class AuthenticationService implements AuthenticationUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final SaccoRepositoryPort saccoRepositoryPort;

    /**
     * Constructs the authentication service with core master schema data repository providers.
     */
    public AuthenticationService(UserRepositoryPort userRepositoryPort, SaccoRepositoryPort saccoRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.saccoRepositoryPort = saccoRepositoryPort;
    }

    /**
     * Validates credentials against security hashes and prepares tenant context maps.
     *
     * @param command The incoming login identifiers containing phone handles and validation pins.
     * @return LoginResult Formatted multi-tenant context details ready for JWT signing engines.
     * @throws IllegalArgumentException If no valid user profile is identified matching credentials.
     * @throws SecurityException If an account status verification check fails.
     */
    @Override
    public LoginResult login(LoginCommand command) {
        // 1. Locate the identity profile inside the global master_schema index
        User user = userRepositoryPort.findByMsisdn(command.msisdn())
                .orElseThrow(() -> new IllegalArgumentException("Invalid authentication credentials provided."));

        // 2. Validate operational readiness states via pure internal Domain verification checks
        if (!user.canLogin()) {
            user.recordFailedLogin();
            userRepositoryPort.save(user);
            throw new SecurityException("Authentication rejected: Account is currently flagged as inactive or locked.");
        }

        // Note: In infrastructure layer we will wire up real password checking filters (BCrypt matchers).
        // This simulates a correct state transition check.
        boolean pinMatches = "1234".equals(command.pin());
        if (!pinMatches) {
            user.recordFailedLogin();
            userRepositoryPort.save(user);
            throw new IllegalArgumentException("Invalid authentication credentials provided.");
        }

        // Reset account metrics on successful connection sequence execution
        user.successfulLogin();
        userRepositoryPort.save(user);

        // 3. Extract tenant parameters from the global registry mapping table
        Sacco parentSacco = saccoRepositoryPort.findById(user.getSaccoId())
                .orElseThrow(() -> new IllegalStateException("Critical corruption: Assigned SACCO map registry entry cannot be resolved."));

        // Note: In infrastructure implementation, a real query will run against sacco_xxx.role_permissions
        // to collect permissions. For now, we seed an immutable placeholder list for structure consistency.
        List<String> userPermissions = List.of("MEMBER_VIEW_BASIC", "LOAN_REQUEST_CREATE");

        UserContext structuredContext = new UserContext(
                user.getUserId(),
                user.getSaccoId(),
                parentSacco.getSchemaName(),
                user.getGlobalRole().name(),
                userPermissions
        );

        return new LoginResult(
                "MOCK_SECURE_JWT_TOKEN_CONTAINER_REPLACE_ME",
                "Bearer",
                3600L,
                structuredContext
        );
    }
}