package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import com.kab.qershi.auth.infrastructure.security.PinValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordService {
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final PinValidator pinValidator;

    public PasswordService(UserRepositoryPort userRepositoryPort,
                           PasswordEncoder passwordEncoder,
                           PinValidator pinValidator) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.pinValidator = pinValidator;
    }

    @Transactional
    public void changePassword(String msisdn, String oldPin, String newPin) {
        // Enforce Core Banking PIN complexity rules before updating credential
        pinValidator.validatePin(newPin, msisdn, oldPin);

        User user = userRepositoryPort.findByMsisdn(msisdn)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPin, user.getCredentialHash())) {
            throw new IllegalArgumentException("Current PIN is incorrect.");
        }

        user.setCredentialHash(passwordEncoder.encode(newPin));
        user.setStatus(UserStatus.ACTIVE);
        userRepositoryPort.save(user);
    }
}