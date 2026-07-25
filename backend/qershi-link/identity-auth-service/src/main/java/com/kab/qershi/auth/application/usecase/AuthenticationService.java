package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.*;
import com.kab.qershi.auth.domain.ports.inbound.AuthenticationUseCase;
import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import com.kab.qershi.auth.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

public class AuthenticationService implements AuthenticationUseCase {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepositoryPort userRepositoryPort;
    private final SaccoRepositoryPort saccoRepositoryPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MessagingPort messagingPort;

    public AuthenticationService(UserRepositoryPort userRepositoryPort, SaccoRepositoryPort saccoRepositoryPort,
                                 JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, MessagingPort messagingPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.saccoRepositoryPort = saccoRepositoryPort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.messagingPort = messagingPort;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        User user = userRepositoryPort.findByMsisdn(command.msisdn())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (user.getStatus() == UserStatus.BLOCKED) {
            log.warn("Blocked user attempt: {}", command.msisdn());
            throw new SecurityException("Account is locked due to multiple failed attempts.");
        }

        if (user.getStatus() == UserStatus.PASSWORD_CHANGE_REQUIRED) {
            return new LoginResult(null, null, 0L, new UserContext(user.getUserId(), user.getSaccoId(), null, "PENDING_PASSWORD", List.of()));
        }

        if (!passwordEncoder.matches(command.pin(), user.getCredentialHash())) {
            user.recordFailedLogin();
            userRepositoryPort.save(user);
            log.warn("Failed login attempt for user: {}. Attempts: {}", command.msisdn(), user.getFailedLoginAttempts());
            messagingPort.sendSms(user.getMsisdn(), "Security Alert: A failed login attempt was detected.");
            throw new IllegalArgumentException("Invalid credentials. " + (3 - user.getFailedLoginAttempts()) + " attempts remaining.");
        }

        user.resetLoginAttempts();
        user.successfulLogin();
        userRepositoryPort.save(user);
        log.info("User login successful: {}", command.msisdn());
        return generateLoginResult(user);
    }

    private LoginResult generateLoginResult(User user) {
        Sacco parentSacco = saccoRepositoryPort.findById(user.getSaccoId())
                .orElseThrow(() -> new IllegalStateException("SACCO registry entry missing."));

        List<String> userPermissions = user.getLocalRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::toAuthority)
                .distinct()
                .toList();

        String token = jwtTokenProvider.createToken(user.getMsisdn(), user.getSaccoId().toString(), userPermissions);

        UserContext context = new UserContext(
                user.getUserId(),
                user.getSaccoId(),
                parentSacco.getSchemaName(),
                user.getGlobalRole().name(),
                userPermissions
        );

        return new LoginResult(token, "Bearer", 3600L, context);
    }
}