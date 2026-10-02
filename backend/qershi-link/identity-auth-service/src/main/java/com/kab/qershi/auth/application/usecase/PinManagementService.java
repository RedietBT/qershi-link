package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.inbound.PinManagementUseCase;
import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.domain.ports.outbound.PasswordEncoderPort;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Application service managing initial PIN dispatches and SMS resend workflows.
 * Pure application service with zero infrastructure imports.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class PinManagementService implements PinManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(PinManagementService.class);

    private final UserRepositoryPort userRepositoryPort;
    private final SaccoRepositoryPort saccoRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final MessagingPort messagingPort;

    public PinManagementService(UserRepositoryPort userRepositoryPort,
                                SaccoRepositoryPort saccoRepositoryPort,
                                PasswordEncoderPort passwordEncoderPort,
                                @Qualifier("notificationGrpcClientAdapter") MessagingPort messagingPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.saccoRepositoryPort = saccoRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.messagingPort = messagingPort;
    }

    @Override
    @Transactional
    public String resendPinByMsisdn(String msisdn) {
        log.info("Request received to resend initial PIN SMS for MSISDN: {}", msisdn);
        User user = userRepositoryPort.findByMsisdn(msisdn)
                .orElseThrow(() -> new IllegalArgumentException("No user account found with phone number " + msisdn));
        return executeResend(user);
    }

    @Override
    @Transactional
    public String resendPinByUserId(UUID userId) {
        log.info("Request received to resend initial PIN SMS for User ID: {}", userId);
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user account found with ID " + userId));
        return executeResend(user);
    }

    private String executeResend(User user) {
        if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.DEACTIVATED) {
            throw new IllegalStateException("Cannot resend PIN. Account status is currently " + user.getStatus());
        }

        // Generate a new 6-digit initial PIN
        String newPin = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);

        user.setCredentialHash(passwordEncoderPort.encode(newPin));
        user.resetLoginAttempts();
        user.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);

        userRepositoryPort.save(user);

        // Fetch SACCO name for personalized SMS greeting
        String saccoName = saccoRepositoryPort.findById(user.getSaccoId())
                .map(Sacco::getSaccoName)
                .orElse("your SACCO");

        String smsMessage = "Welcome to " + saccoName + "! Your initial login PIN has been reset. Your new PIN is: " + newPin;
        try {
            messagingPort.sendSms(user.getMsisdn(), smsMessage);
            log.info("Resent initial PIN SMS notification to {}", user.getMsisdn());
        } catch (Exception e) {
            log.error("Failed to resend SMS to {}: {}", user.getMsisdn(), e.getMessage());
        }

        return "New initial PIN successfully generated and sent via SMS to " + user.getMsisdn() + ".";
    }
}
