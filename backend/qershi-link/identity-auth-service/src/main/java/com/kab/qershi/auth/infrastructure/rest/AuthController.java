package com.kab.qershi.auth.infrastructure.rest;

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

import com.kab.qershi.auth.infrastructure.security.JwtTokenProvider;
import com.kab.qershi.auth.infrastructure.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Inbound Edge Adapter exposing RESTful identity context and verification endpoints.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Engine", description = "Provides connection handling and profile context extraction rules for React and Flutter applications")
public class AuthController {

    private final AuthenticationUseCase authenticationUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(AuthenticationUseCase authenticationUseCase,
                          JwtTokenProvider jwtTokenProvider,
                          TokenBlacklistService tokenBlacklistService) {
        this.authenticationUseCase = authenticationUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

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
        LoginCommand command = new LoginCommand(request.msisdn(), request.pin());
        LoginResult result = authenticationUseCase.login(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Execute Session Logout",
            description = "Revokes the active Bearer JWT token by adding its unique token ID (jti) to the Redis revocation blacklist."
    )
    @ApiResponse(responseCode = "200", description = "Successfully logged out and session token revoked.")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                String jti = claims.getId();
                Date expiration = claims.getExpiration();
                if (jti != null && expiration != null) {
                    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                    tokenBlacklistService.blacklistToken(jti, remainingMillis);
                }
            } catch (Exception ignored) {
                // Token invalid or already expired
            }
        }
        return ResponseEntity.ok("Successfully logged out.");
    }
}