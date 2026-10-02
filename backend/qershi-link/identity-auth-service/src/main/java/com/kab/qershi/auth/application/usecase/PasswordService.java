package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.inbound.PasswordManagementUseCase;
import com.kab.qershi.auth.domain.ports.inbound.SystemAuditUseCase;
import com.kab.qershi.auth.domain.ports.outbound.PasswordEncoderPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import com.kab.qershi.auth.domain.service.PinValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for user password and PIN management.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@Service
public class PasswordService implements PasswordManagementUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final PinValidator pinValidator;
    private final SystemAuditUseCase systemAuditUseCase;

    public PasswordService(UserRepositoryPort userRepositoryPort,
                           PasswordEncoderPort passwordEncoderPort,
                           SystemAuditUseCase systemAuditUseCase) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.pinValidator = new PinValidator();
        this.systemAuditUseCase = systemAuditUseCase;
    }

    @Override
    @Transactional
    public void changePassword(String msisdn, String oldPin, String newPin) {
        // Enforce Core Banking PIN complexity rules before updating credential
        pinValidator.validatePin(newPin, msisdn, oldPin);

        User user = userRepositoryPort.findByMsisdn(msisdn)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoderPort.matches(oldPin, user.getCredentialHash())) {
            systemAuditUseCase.recordAuditLog(user.getUserId(), user.getSaccoId(), "PIN_ROTATION_FAILED", "USER", "FAILURE", null, "Current PIN validation failed");
            throw new IllegalArgumentException("Current PIN is incorrect.");
        }

        user.setCredentialHash(passwordEncoderPort.encode(newPin));
        user.setStatus(UserStatus.ACTIVE);
        userRepositoryPort.save(user);

        systemAuditUseCase.recordAuditLog(user.getUserId(), user.getSaccoId(), "PIN_ROTATED", "USER", "SUCCESS", null, "User PIN successfully rotated");
    }
}