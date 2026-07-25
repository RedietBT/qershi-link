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
        // Retrieves the MSISDN from the security context (set by the JWT filter)
        String msisdn = SecurityContextHolder.getContext().getAuthentication().getName();

        passwordService.changePassword(msisdn, request.oldPin(), request.newPin());

        return ResponseEntity.ok("PIN updated successfully.");
    }
}