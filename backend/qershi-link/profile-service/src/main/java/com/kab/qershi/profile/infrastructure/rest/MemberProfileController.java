package com.kab.qershi.profile.infrastructure.rest;

import com.kab.qershi.profile.domain.model.MemberAddress;
import com.kab.qershi.profile.domain.model.MemberEmployment;
import com.kab.qershi.profile.domain.model.MemberProfile;
import com.kab.qershi.profile.domain.model.MemberStatus;
import com.kab.qershi.profile.domain.ports.inbound.ProfileManagementUseCase;
import com.kab.qershi.profile.infrastructure.rest.dto.ApiResponse;
import com.kab.qershi.profile.infrastructure.rest.dto.ApproveOnboardingRequest;
import com.kab.qershi.profile.infrastructure.rest.dto.ChangeStatusRequest;
import com.kab.qershi.profile.infrastructure.rest.dto.CreateProfileRequest;
import com.kab.qershi.profile.infrastructure.rest.dto.MemberProfileResponse;
import com.kab.qershi.profile.infrastructure.rest.dto.SaveAddressRequest;
import com.kab.qershi.profile.infrastructure.rest.dto.SaveEmploymentRequest;
import com.kab.qershi.profile.infrastructure.rest.dto.UpdateDemographicsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * REST API Controller for SACCO Member Profile Lifecycle Management.
 * Secured with fine-grained RBAC permission checks (@PreAuthorize("hasAuthority(...)")).
 * Automatically extracts maker submittedByUserId from JWT authentication.
 *
 * @author KAB Digital Solution PLC
 * @version 1.3.0
 */
@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Member Profile Management", description = "Member Onboarding, Demographics, Address, Employment, and Maker-Checker Governance APIs")
public class MemberProfileController {

    private static final Logger log = LoggerFactory.getLogger(MemberProfileController.class);
    private final ProfileManagementUseCase profileManagementUseCase;

    public MemberProfileController(ProfileManagementUseCase profileManagementUseCase) {
        this.profileManagementUseCase = profileManagementUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MEMBER_CREATE')")
    @Operation(summary = "Register Member Profile", description = "Creates a new SACCO member profile linked to userId with auto-generated Member ID. SubmittedByUserId is automatically extracted from JWT.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Member profile created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation constraint failure or invalid payload format", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: Profile already exists for user ID", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createProfile(
            @Valid @RequestBody CreateProfileRequest request,
            Authentication authentication) {
        
        UUID submittedByUserId = extractUserIdFromAuthentication(authentication);

        MemberProfile profile = profileManagementUseCase.createMemberProfile(
                request.getUserId(),
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName(),
                request.getGender(),
                request.getDateOfBirth(),
                request.getMaritalStatus(),
                submittedByUserId
        );
        MemberProfileResponse response = MemberProfileResponse.fromDomain(profile, null, null, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Member profile created successfully", response));
    }

    @PutMapping("/{userId}/demographics")
    @PreAuthorize("hasAuthority('MEMBER_UPDATE')")
    @Operation(summary = "Update Demographics", description = "Updates member names, date of birth, gender, and marital status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Demographics updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or alphabetic name constraint failure", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Member profile not found for user ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateDemographics(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId,
            @Valid @RequestBody UpdateDemographicsRequest request) {
        MemberProfile updated = profileManagementUseCase.updateDemographics(
                userId,
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName(),
                request.getGender(),
                request.getDateOfBirth(),
                request.getMaritalStatus()
        );
        MemberProfileResponse response = MemberProfileResponse.fromDomain(updated, null, null, null);
        return ResponseEntity.ok(ApiResponse.ok("Demographics updated successfully", response));
    }

    @PostMapping("/{userId}/address")
    @PreAuthorize("hasAuthority('MEMBER_UPDATE')")
    @Operation(summary = "Save Contact Address", description = "Saves or updates physical residence location and E.164 primary phone handles.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contact address saved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid phone E.164 format or region data missing", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cannot set address. Member profile not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MemberAddress>> saveAddress(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId,
            @Valid @RequestBody SaveAddressRequest request) {
        MemberAddress address = profileManagementUseCase.saveContactAddress(
                userId,
                request.getPrimaryPhone(),
                request.getSecondaryPhone(),
                request.getEmail(),
                request.getRegion(),
                request.getZoneSubcity(),
                request.getWoreda(),
                request.getHouseNumber()
        );
        return ResponseEntity.ok(ApiResponse.ok("Contact address saved successfully", address));
    }

    @PostMapping("/{userId}/employment")
    @PreAuthorize("hasAuthority('MEMBER_UPDATE')")
    @Operation(summary = "Save Employment Profile", description = "Saves or updates member economic sector, employer, monthly income, and TIN number.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employment profile saved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Negative monthly income or invalid 10-digit TIN format", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cannot set employment. Member profile not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MemberEmployment>> saveEmployment(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId,
            @Valid @RequestBody SaveEmploymentRequest request) {
        MemberEmployment employment = profileManagementUseCase.saveEmploymentProfile(
                userId,
                request.getOccupationSector(),
                request.getEmployerName(),
                request.getMonthlyIncome(),
                request.getTinNumber()
        );
        return ResponseEntity.ok(ApiResponse.ok("Employment profile saved successfully", employment));
    }

    @PutMapping("/{userId}/approve")
    @PreAuthorize("hasAuthority('MEMBER_APPROVE')")
    @Operation(summary = "Approve Member Onboarding", description = "Four-Eye Principle (Maker-Checker) supervisor approval to activate member profile.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member onboarding approved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Supervisor ID missing or invalid approval payload", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Member profile not found for user ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MemberProfileResponse>> approveOnboarding(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId,
            @Valid @RequestBody ApproveOnboardingRequest request) {
        MemberProfile approved = profileManagementUseCase.approveMemberOnboarding(
                userId,
                request.getSupervisorId(),
                request.getRemarks()
        );
        MemberProfileResponse response = MemberProfileResponse.fromDomain(approved, null, null, null);
        return ResponseEntity.ok(ApiResponse.ok("Member onboarding approved successfully", response));
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('MEMBER_UPDATE')")
    @Operation(summary = "Change Member Status", description = "Transitions member lifecycle status (ACTIVE, SUSPENDED, CLOSED).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member status updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid MemberStatus enum value", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Member profile not found for user ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MemberProfileResponse>> changeStatus(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId,
            @Valid @RequestBody ChangeStatusRequest request) {
        MemberProfile updated = profileManagementUseCase.changeMemberStatus(userId, request.getStatus());
        MemberProfileResponse response = MemberProfileResponse.fromDomain(updated, null, null, null);
        return ResponseEntity.ok(ApiResponse.ok("Member status updated successfully", response));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('MEMBER_VIEW_BASIC')")
    @Operation(summary = "Get Profile by User ID", description = "Fetches member profile details by user ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile details retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Member profile not found for user ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfileByUserId(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId) {
        Optional<MemberProfile> profileOpt = profileManagementUseCase.getProfileByUserId(userId);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Member profile not found for user ID: " + userId));
        }
        MemberProfileResponse response = MemberProfileResponse.fromDomain(profileOpt.get(), null, null, null);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/member-no/{memberNo}")
    @PreAuthorize("hasAuthority('MEMBER_VIEW_BASIC')")
    @Operation(summary = "Get Profile by Member Number", description = "Searches member profile by structured member number (e.g. QL-2026-000142).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile details retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Member profile not found for Member No", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfileByMemberNo(
            @Parameter(description = "Structured member number string", required = true) @PathVariable String memberNo) {
        Optional<MemberProfile> profileOpt = profileManagementUseCase.getProfileByMemberNo(memberNo);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Member profile not found for Member No: " + memberNo));
        }
        MemberProfileResponse response = MemberProfileResponse.fromDomain(profileOpt.get(), null, null, null);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MEMBER_VIEW_BASIC')")
    @Operation(summary = "List Member Profiles", description = "Lists member profiles filtered by optional lifecycle status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member profiles list retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<MemberProfileResponse>>> getAllProfiles(
            @Parameter(description = "Optional filter by MemberStatus") @RequestParam(required = false) MemberStatus status) {
        List<MemberProfile> profiles = profileManagementUseCase.getAllProfiles(status);
        List<MemberProfileResponse> responseList = profiles.stream()
                .map(p -> MemberProfileResponse.fromDomain(p, null, null, null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    /**
     * Extracts authenticated user's UUID from Spring Security Authentication token.
     */
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
            } catch (IllegalArgumentException e) {
                log.warn("Principal string [{}] is not a valid UUID format", str);
            }
        }
        if (authentication.getDetails() != null) {
            try {
                return UUID.fromString(authentication.getDetails().toString().trim());
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }
}
