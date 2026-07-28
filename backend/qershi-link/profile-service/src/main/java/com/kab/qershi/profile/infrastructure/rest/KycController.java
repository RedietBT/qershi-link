package com.kab.qershi.profile.infrastructure.rest;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API Controller for Government Identity Document Submissions and KYC Verification.
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
    @Operation(summary = "Submit KYC Document", description = "Submits a government identity document (National ID, Passport, Kebele ID, Driving License) for member verification.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "KYC document submitted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ID number or IdType validation constraint failure", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cannot submit KYC. Member profile not found for user ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<KycIdentificationResponse>> submitKycIdentification(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId,
            @Valid @RequestBody SubmitKycRequest request) {
        MemberIdentification identification = kycVerificationUseCase.submitKycIdentification(
                userId,
                request.getIdType(),
                request.getIdNumber(),
                request.getIssueDate(),
                request.getExpiryDate(),
                request.getIssuingAuthority()
        );
        KycIdentificationResponse response = KycIdentificationResponse.fromDomain(identification);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("KYC document submitted successfully", response));
    }

    @GetMapping("/{userId}/identifications")
    @Operation(summary = "List Member KYC Documents", description = "Lists all government identity verification documents submitted for a member.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC document list retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
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
    @Operation(summary = "Verify KYC Document", description = "Supervisor approves identity document status to VERIFIED.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC document verified successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Supervisor ID missing or invalid verification payload", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Identification document not found for ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<KycIdentificationResponse>> verifyKycIdentification(
            @Parameter(description = "UUID of the identification document", required = true) @PathVariable UUID identificationId,
            @Valid @RequestBody VerifyKycRequest request) {
        MemberIdentification verified = kycVerificationUseCase.verifyKycIdentification(
                identificationId,
                request.getSupervisorId(),
                request.getNotes()
        );
        return ResponseEntity.ok(ApiResponse.ok("KYC document verified successfully", KycIdentificationResponse.fromDomain(verified)));
    }

    @PutMapping("/identifications/{identificationId}/reject")
    @Operation(summary = "Reject KYC Document", description = "Supervisor rejects identity document with rejection audit notes.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC document rejected", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Supervisor ID missing or invalid rejection payload", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Identification document not found for ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<KycIdentificationResponse>> rejectKycIdentification(
            @Parameter(description = "UUID of the identification document", required = true) @PathVariable UUID identificationId,
            @Valid @RequestBody VerifyKycRequest request) {
        MemberIdentification rejected = kycVerificationUseCase.rejectKycIdentification(
                identificationId,
                request.getSupervisorId(),
                request.getNotes()
        );
        return ResponseEntity.ok(ApiResponse.ok("KYC document rejected", KycIdentificationResponse.fromDomain(rejected)));
    }
}
