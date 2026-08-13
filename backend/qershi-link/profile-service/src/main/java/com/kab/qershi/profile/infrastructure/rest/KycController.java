package com.kab.qershi.profile.infrastructure.rest;

import com.kab.qershi.profile.domain.model.KycStatus;
import com.kab.qershi.profile.domain.model.MemberIdentification;
import com.kab.qershi.profile.domain.ports.inbound.KycVerificationUseCase;
import com.kab.qershi.profile.infrastructure.rest.dto.ApiResponse;
import com.kab.qershi.profile.infrastructure.rest.dto.KycIdentificationResponse;
import com.kab.qershi.profile.infrastructure.rest.dto.SubmitKycRequest;
import com.kab.qershi.profile.infrastructure.rest.dto.VerifyKycRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API Controller for Government Identity Document Submissions and KYC Verification.
 * Secured with fine-grained RBAC permission checks (@PreAuthorize).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/kyc")
@Tag(name = "KYC Verification", description = "Government ID Document Submissions and Supervisor Verification APIs")
public class KycController {

    private final KycVerificationUseCase kycVerificationUseCase;

    public KycController(KycVerificationUseCase kycVerificationUseCase) {
        this.kycVerificationUseCase = kycVerificationUseCase;
    }

    @PostMapping("/{userId}/identifications")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('KYC_SUBMIT')")
    @Operation(summary = "Submit KYC Identification Document", description = "Stores official identity document details for a member profile.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "KYC identification document submitted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid ID payload", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<KycIdentificationResponse>> submitIdentification(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId,
            @Valid @RequestBody SubmitKycRequest request) {
        MemberIdentification saved = kycVerificationUseCase.submitKycIdentification(
                userId,
                request.getIdType(),
                request.getIdNumber(),
                request.getIssueDate(),
                request.getExpiryDate(),
                request.getIssuingAuthority()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("KYC identification document submitted successfully", KycIdentificationResponse.fromDomain(saved)));
    }

    @GetMapping("/identifications")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('KYC_VIEW')")
    @Operation(summary = "Get All Identification Documents", description = "Retrieves all member identity documents across the SACCO. Optionally filters by KYC verification status (UNVERIFIED, VERIFIED, REJECTED).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC documents retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<KycIdentificationResponse>>> getAllIdentifications(
            @Parameter(description = "Optional filter by KYC status (UNVERIFIED, VERIFIED, REJECTED)")
            @RequestParam(required = false) KycStatus status) {
        List<MemberIdentification> list = kycVerificationUseCase.getAllIdentifications(status);
        List<KycIdentificationResponse> responseList = list.stream()
                .map(KycIdentificationResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/{userId}/identifications")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('KYC_VIEW')")
    @Operation(summary = "List Member KYC Documents", description = "Lists all government identity document submissions for a specific member.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member KYC documents retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<KycIdentificationResponse>>> getIdentificationsByUserId(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId) {
        List<MemberIdentification> list = kycVerificationUseCase.getIdentificationsByUserId(userId);
        List<KycIdentificationResponse> responseList = list.stream()
                .map(KycIdentificationResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/identifications/{identificationId}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('KYC_VIEW')")
    @Operation(summary = "Get KYC Document Details", description = "Fetches details of a specific identity document by identification ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC document details retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Identification document not found for ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<KycIdentificationResponse>> getIdentificationById(
            @Parameter(description = "UUID of the identification document", required = true) @PathVariable UUID identificationId) {
        Optional<MemberIdentification> opt = kycVerificationUseCase.getIdentificationById(identificationId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Identification document not found for ID: " + identificationId));
        }
        return ResponseEntity.ok(ApiResponse.ok(KycIdentificationResponse.fromDomain(opt.get())));
    }

    @PutMapping("/identifications/{identificationId}/verify")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('KYC_VERIFY')")
    @Operation(summary = "Verify KYC Document", description = "Supervisor approves identity document status to VERIFIED. Supervisor ID is extracted from JWT.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC document verified successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Supervisor ID missing or invalid verification payload", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Identification document not found for ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<KycIdentificationResponse>> verifyKycIdentification(
            @Parameter(description = "UUID of the identification document", required = true) @PathVariable UUID identificationId,
            @RequestBody(required = false) VerifyKycRequest request,
            Authentication authentication) {
        UUID supervisorId = extractUserIdFromAuthentication(authentication);
        String notes = (request != null) ? request.getNotes() : null;

        MemberIdentification verified = kycVerificationUseCase.verifyKycIdentification(
                identificationId,
                supervisorId,
                notes
        );
        return ResponseEntity.ok(ApiResponse.ok("KYC document verified successfully", KycIdentificationResponse.fromDomain(verified)));
    }

    @PutMapping("/identifications/{identificationId}/reject")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('KYC_VERIFY')")
    @Operation(summary = "Reject KYC Document", description = "Supervisor rejects identity document with rejection audit notes. Supervisor ID is extracted from JWT.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC document rejected", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Supervisor ID missing or invalid rejection payload", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Identification document not found for ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<KycIdentificationResponse>> rejectKycIdentification(
            @Parameter(description = "UUID of the identification document", required = true) @PathVariable UUID identificationId,
            @RequestBody(required = false) VerifyKycRequest request,
            Authentication authentication) {
        UUID supervisorId = extractUserIdFromAuthentication(authentication);
        String notes = (request != null) ? request.getNotes() : null;

        MemberIdentification rejected = kycVerificationUseCase.rejectKycIdentification(
                identificationId,
                supervisorId,
                notes
        );
        return ResponseEntity.ok(ApiResponse.ok("KYC document rejected", KycIdentificationResponse.fromDomain(rejected)));
    }

    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        if (principal instanceof String str) {
            try {
                return UUID.fromString(str.trim());
            } catch (IllegalArgumentException ignored) {}
        }
        if (authentication.getDetails() != null) {
            try {
                return UUID.fromString(authentication.getDetails().toString().trim());
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }
}
