package com.kab.qershi.auth.infrastructure.rest;

// 🛠️ FIXED: Pointed these imports to the correct inbound port package location
import com.kab.qershi.auth.domain.ports.inbound.AuthenticationUseCase;
import com.kab.qershi.auth.domain.ports.inbound.AuthenticationUseCase.LoginCommand;
import com.kab.qershi.auth.domain.ports.inbound.AuthenticationUseCase.LoginResult;
import com.kab.qershi.auth.infrastructure.rest.dto.LoginRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound Edge Adapter exposing RESTful identity context and verification endpoints.
 * Maps authenticated credentials down into application pipelines for validation and token rendering.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Engine", description = "Provides connection handling and profile context extraction rules for React and Flutter applications")
public class AuthController {

    private final AuthenticationUseCase authenticationUseCase;

    /**
     * Constructs the authentication entry router adapter bound to primary engine interfaces.
     *
     * @param authenticationUseCase The application core execution engine handling credentials routing.
     */
    public AuthController(AuthenticationUseCase authenticationUseCase) {
        this.authenticationUseCase = authenticationUseCase;
    }

    /**
     * Section 1.1.1.6.2: Multi-Tenant Credentials Verification Entrypoint
     * Validates inbound security payloads and emits isolated tenant parameters if valid.
     *
     * @param request The validated login credentials payload block mapping user identifiers.
     * @return ResponseEntity Enclosing the signed security token contexts wrapper.
     */
    @PostMapping("/login")
    @Operation(
            summary = "Execute Multi-Tenant Identity Verification Request",
            description = "Processes security credentials, tracks metrics profiles inside the master schema registry, and compiles authorized role strings."
    )
    @ApiResponse(responseCode = "200", description = "Account credentials authenticated successfully.",
            content = @Content(schema = @Schema(implementation = LoginResult.class)))
    @ApiResponse(responseCode = "400", description = "Provided input validation elements or arguments are malformed.")
    @ApiResponse(responseCode = "401", description = "Authentication rejected: Invalid phone registry handle or bad PIN mapping sequence.")
    @ApiResponse(responseCode = "423", description = "Security state lock active: Account flagged as locked or temporarily frozen due to excessive login failure triggers.")
    public ResponseEntity<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        // Transpile validated REST payload data object down to core application command records
        LoginCommand command = new LoginCommand(request.msisdn(), request.pin());

        LoginResult result = authenticationUseCase.login(command);
        return ResponseEntity.ok(result);
    }
}