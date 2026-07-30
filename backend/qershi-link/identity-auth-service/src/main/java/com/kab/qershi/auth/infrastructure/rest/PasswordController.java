package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.application.usecase.PasswordService;
import com.kab.qershi.auth.infrastructure.rest.dto.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user authentication PIN management and password rotation.
 * Supports both unauthenticated first-time password rotation using MSISDN + initial PIN,
 * and authenticated user PIN rotation via Bearer JWT.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Engine", description = "Endpoints for managing identity security and PIN rotation")
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Rotate User PIN",
            description = "Validates the current PIN and updates it to a new 6-digit code. Required for initial login flows."
    )
    @ApiResponse(responseCode = "200", description = "PIN updated successfully.")
    @ApiResponse(responseCode = "400", description = "Invalid current PIN or validation error.")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String msisdn = request.msisdn();

        // If MSISDN is omitted in the request body, extract it from SecurityContext (JWT authentication)
        if (msisdn == null || msisdn.isBlank()) {
            if (SecurityContextHolder.getContext().getAuthentication() != null
                    && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                    && !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
                msisdn = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        }

        if (msisdn == null || msisdn.isBlank()) {
            throw new IllegalArgumentException("Phone number (msisdn) is required in the request body or via Bearer Authorization header.");
        }

        passwordService.changePassword(msisdn, request.oldPin(), request.newPin());

        return ResponseEntity.ok("PIN updated successfully.");
    }
}