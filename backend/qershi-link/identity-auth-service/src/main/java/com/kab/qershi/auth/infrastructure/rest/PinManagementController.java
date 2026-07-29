package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataUserRepository;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;
import com.kab.qershi.auth.infrastructure.rest.dto.ResendPinRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Dedicated REST controller providing standalone global endpoints for initial PIN dispatches and SMS resend triggers.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/pin")
@Tag(name = "PIN & Credential Operations", description = "Standalone global endpoints for initial PIN dispatches and SMS resend triggers")
public class PinManagementController {

    private static final Logger log = LoggerFactory.getLogger(PinManagementController.class);
    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessagingPort messagingPort;

    public PinManagementController(SpringDataUserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   MessagingPort messagingPort) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messagingPort = messagingPort;
    }

    @PostMapping("/resend")
    @Transactional
    @Operation(
            summary = "Resend Initial Login PIN via SMS (Global)",
            description = "Generates a fresh 6-digit initial PIN, updates the credential hash in database, and dispatches an SMS via AfroMessage."
    )
    @ApiResponse(responseCode = "200", description = "Fresh initial PIN generated and SMS notification dispatched successfully.")
    @ApiResponse(responseCode = "404", description = "No user account registered under the specified MSISDN.")
    public ResponseEntity<String> resendPinByMsisdn(@Valid @RequestBody ResendPinRequest request) {
        log.info("Global request received to resend initial PIN SMS for MSISDN: {}", request.msisdn());

        UserEntity userEntity = userRepository.findByMsisdn(request.msisdn())
                .orElseThrow(() -> new IllegalArgumentException("No user account found with phone number " + request.msisdn()));

        return executeResendPin(userEntity);
    }

    @PostMapping("/resend/{userId}")
    @Transactional
    @Operation(
            summary = "Resend Initial Login PIN via SMS by User ID (Global)",
            description = "Generates a fresh 6-digit initial PIN for the specified User ID, updates database, and dispatches an SMS via AfroMessage."
    )
    @ApiResponse(responseCode = "200", description = "Fresh initial PIN generated and SMS notification dispatched successfully.")
    @ApiResponse(responseCode = "404", description = "No user account registered under the specified User ID.")
    public ResponseEntity<String> resendPinByUserId(@PathVariable UUID userId) {
        log.info("Global request received to resend initial PIN SMS for User ID: {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user account found with ID " + userId));

        return executeResendPin(userEntity);
    }

    private ResponseEntity<String> executeResendPin(UserEntity userEntity) {
        if (userEntity.getStatus() == UserStatus.BLOCKED || userEntity.getStatus() == UserStatus.DEACTIVATED) {
            throw new IllegalStateException("Cannot resend PIN. Account status is currently " + userEntity.getStatus());
        }

        // Generate a new 6-digit initial PIN to replace the previous one
        String newPin = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);

        userEntity.setCredentialHash(passwordEncoder.encode(newPin));
        userEntity.setFailedLoginAttempts(0);
        userEntity.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);

        userRepository.save(userEntity);

        // Dispatch new PIN via AfroMessage SMS gateway
        String smsMessage = "Welcome to Qershi Link! Your initial login PIN has been reset. Your new PIN is: " + newPin;
        try {
            messagingPort.sendSms(userEntity.getMsisdn(), smsMessage);
            log.info("Resent initial PIN SMS notification to {}", userEntity.getMsisdn());
        } catch (Exception e) {
            log.error("Failed to resend SMS to {}: {}", userEntity.getMsisdn(), e.getMessage());
        }

        return ResponseEntity.ok("New initial PIN (" + newPin + ") successfully generated and sent via SMS to " + userEntity.getMsisdn() + ".");
    }
}
