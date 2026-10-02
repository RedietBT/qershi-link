package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.ports.inbound.PinManagementUseCase;
import com.kab.qershi.auth.infrastructure.rest.dto.ResendPinRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Standalone global endpoints for initial PIN dispatches and SMS resend triggers.
 * Injects inbound port PinManagementUseCase.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/pin")
@Tag(name = "PIN & Credential Operations", description = "Standalone global endpoints for initial PIN dispatches and SMS resend triggers")
public class PinManagementController {

    private final PinManagementUseCase pinManagementUseCase;

    public PinManagementController(PinManagementUseCase pinManagementUseCase) {
        this.pinManagementUseCase = pinManagementUseCase;
    }

    @PostMapping("/resend")
    @Operation(
            summary = "Resend Initial Login PIN via SMS (Global)",
            description = "Generates a fresh 6-digit initial PIN, updates credential hash in database, and dispatches an SMS."
    )
    @ApiResponse(responseCode = "200", description = "Fresh initial PIN generated and SMS notification dispatched successfully.")
    @ApiResponse(responseCode = "404", description = "No user account registered under the specified MSISDN.")
    public ResponseEntity<String> resendPinByMsisdn(@Valid @RequestBody ResendPinRequest request) {
        String result = pinManagementUseCase.resendPinByMsisdn(request.msisdn());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/resend/{userId}")
    @Operation(
            summary = "Resend Initial Login PIN via SMS by User ID (Global)",
            description = "Generates a fresh 6-digit initial PIN for specified User ID, updates database, and dispatches an SMS."
    )
    @ApiResponse(responseCode = "200", description = "Fresh initial PIN generated and SMS notification dispatched successfully.")
    @ApiResponse(responseCode = "404", description = "No user account registered under the specified User ID.")
    public ResponseEntity<String> resendPinByUserId(@PathVariable UUID userId) {
        String result = pinManagementUseCase.resendPinByUserId(userId);
        return ResponseEntity.ok(result);
    }
}
