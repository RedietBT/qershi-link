package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.ports.inbound.AccountOpeningUseCase;
import com.kab.qershi.account.infrastructure.rest.dto.OpenAccountRequest;
import com.kab.qershi.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing Member Core Account Management APIs.
 * Includes Four-Eye approval and tenant-isolated phone number search.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account Management", description = "Endpoints for opening member accounts, Four-Eye approvals, and tenant-isolated lookups.")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountOpeningUseCase accountOpeningUseCase;
    private final com.kab.qershi.account.application.usecase.AccountDormancyService accountDormancyService;

    public AccountController(AccountOpeningUseCase accountOpeningUseCase,
                             com.kab.qershi.account.application.usecase.AccountDormancyService accountDormancyService) {
        this.accountOpeningUseCase = accountOpeningUseCase;
        this.accountDormancyService = accountDormancyService;
    }

    @PostMapping("/{accountNo}/reactivation/request")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'CUSTOMER_SERVICE') or hasAuthority('ACCOUNT_OPEN')")
    @Operation(summary = "Initiate KYC Reactivation (Maker)", description = "Maker submits in-person KYC re-verification documents to reactivate a DORMANT account.")
    public ResponseEntity<ApiResponse<com.kab.qershi.account.infrastructure.persistence.AccountEntity>> initiateReactivation(
            @PathVariable String accountNo,
            @Valid @RequestBody com.kab.qershi.account.infrastructure.rest.dto.KycReactivationRequest request,
            Authentication authentication) {
        UUID makerUserId = parseUserId(authentication);
        com.kab.qershi.account.infrastructure.persistence.AccountEntity entity =
                accountDormancyService.initiateKycReactivation(accountNo, makerUserId, request);
        return ResponseEntity.ok(ApiResponse.success(entity, "KYC reactivation request submitted. Pending supervisor Four-Eye authorization."));
    }

    @PutMapping("/{accountNo}/reactivation/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER') or hasAuthority('ACCOUNT_APPROVE')")
    @Operation(summary = "Approve KYC Reactivation (Checker)", description = "Checker supervisor validates in-person verification and restores account to ACTIVE.")
    public ResponseEntity<ApiResponse<com.kab.qershi.account.infrastructure.persistence.AccountEntity>> approveReactivation(
            @PathVariable String accountNo,
            @Valid @RequestBody com.kab.qershi.account.infrastructure.rest.dto.KycApprovalRequest request,
            Authentication authentication) {
        UUID checkerUserId = parseUserId(authentication);
        com.kab.qershi.account.infrastructure.persistence.AccountEntity entity =
                accountDormancyService.approveKycReactivation(accountNo, checkerUserId, request);
        return ResponseEntity.ok(ApiResponse.success(entity, "Account " + accountNo + " successfully reactivated to ACTIVE status."));
    }

    @PutMapping("/{accountNo}/reactivation/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER') or hasAuthority('ACCOUNT_APPROVE')")
    @Operation(summary = "Reject KYC Reactivation (Checker)", description = "Checker supervisor rejects in-person KYC reactivation.")
    public ResponseEntity<ApiResponse<com.kab.qershi.account.infrastructure.persistence.AccountEntity>> rejectReactivation(
            @PathVariable String accountNo,
            @Valid @RequestBody com.kab.qershi.account.infrastructure.rest.dto.KycApprovalRequest request,
            Authentication authentication) {
        UUID checkerUserId = parseUserId(authentication);
        com.kab.qershi.account.infrastructure.persistence.AccountEntity entity =
                accountDormancyService.rejectKycReactivation(accountNo, checkerUserId, request);
        return ResponseEntity.ok(ApiResponse.success(entity, "Account " + accountNo + " reactivation request rejected. Account remains DORMANT."));
    }

    @GetMapping("/dormant")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "List Dormant Accounts", description = "Retrieves all accounts currently marked as DORMANT (>180 days inactivity).")
    public ResponseEntity<ApiResponse<List<com.kab.qershi.account.infrastructure.persistence.AccountEntity>>> getDormantAccounts() {
        List<com.kab.qershi.account.infrastructure.persistence.AccountEntity> dormant = accountDormancyService.getDormantAccounts();
        return ResponseEntity.ok(ApiResponse.success(dormant, "Retrieved " + dormant.size() + " dormant accounts."));
    }

    @GetMapping("/reactivations/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR') or hasAuthority('ACCOUNT_APPROVE')")
    @Operation(summary = "List Pending Reactivations", description = "Retrieves dormant accounts currently awaiting supervisor Four-Eye authorization.")
    public ResponseEntity<ApiResponse<List<com.kab.qershi.account.infrastructure.persistence.AccountEntity>>> getPendingReactivations() {
        List<com.kab.qershi.account.infrastructure.persistence.AccountEntity> pending = accountDormancyService.getPendingReactivations();
        return ResponseEntity.ok(ApiResponse.success(pending, "Retrieved " + pending.size() + " pending reactivation requests."));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ACCOUNT_OPEN', 'ACCOUNT_CREATE')")
    @Operation(summary = "Open New Member Account", description = "Opens a core ledger account for a member and generates an ISO Luhn account number (e.g. 0001-002-101-0001429).")
    public ResponseEntity<ApiResponse<Account>> openAccount(@Valid @RequestBody OpenAccountRequest request) {
        Account account = accountOpeningUseCase.openAccount(
                request.getUserId(),
                request.getBranchCode(),
                request.getProductCode()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "Member account opened successfully. Status: PENDING_APPROVAL"));
    }

    @PutMapping("/{accountNo}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('ACCOUNT_APPROVE')")
    @Operation(summary = "Approve Account Opening (Four-Eye Checker)", description = "Activates a pending account via Four-Eye Maker-Checker approval workflow.")
    public ResponseEntity<ApiResponse<Account>> approveAccount(@PathVariable String accountNo, Authentication authentication) {
        UUID checkerUserId = parseUserId(authentication);
        Account approved = accountOpeningUseCase.approveAccount(accountNo, checkerUserId);
        return ResponseEntity.ok(ApiResponse.success(approved, "Account " + accountNo + " approved and activated successfully."));
    }

    @GetMapping("/{accountNo}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Get Account Details", description = "Retrieves account ledger balances and status by account number.")
    public ResponseEntity<ApiResponse<Account>> getAccountByNo(@PathVariable String accountNo) {
        Account account = accountOpeningUseCase.getAccountByNo(accountNo);
        return ResponseEntity.ok(ApiResponse.success(account, "Account details retrieved successfully."));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Get Member Accounts", description = "Retrieves all accounts owned by a specific member within the active SACCO tenant.")
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByUserId(@PathVariable UUID userId) {
        List<Account> accounts = accountOpeningUseCase.getAccountsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(accounts, "Retrieved " + accounts.size() + " accounts for member."));
    }

    @GetMapping("/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Find Accounts by Phone Number", description = "Tenant-isolated lookup returning accounts linked to member phone number in the active SACCO tenant.")
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByPhoneNumber(@PathVariable String phoneNumber) {
        List<Account> accounts = accountOpeningUseCase.getAccountsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(ApiResponse.success(accounts, "Found " + accounts.size() + " accounts linked to phone number " + phoneNumber + " in this SACCO."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR') or hasAnyAuthority('ACCOUNT_VIEW_ALL', 'ACCOUNT_VIEW')")
    @Operation(summary = "List All Accounts", description = "Retrieves all member accounts across the active SACCO tenant.")
    public ResponseEntity<ApiResponse<List<Account>>> getAllAccounts() {
        List<Account> accounts = accountOpeningUseCase.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts, "Retrieved " + accounts.size() + " accounts."));
    }

    /**
     * Resolves the authenticated operator's user ID from the JWT security context.
     * Throws AccessDeniedException if the identity cannot be resolved — this prevents
     * account operations from being recorded with a random or fabricated operator ID,
     * which would break the audit trail integrity required by Core Banking standards.
     *
     * @param auth The Spring Security Authentication object.
     * @return UUID The verified operator user ID.
     * @throws AccessDeniedException if the operator identity cannot be determined.
     */
    private UUID parseUserId(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UUID uuid) {
                return uuid;
            }
            try {
                return UUID.fromString(principal.toString());
            } catch (Exception ignored) {}
        }
        // Audit trail integrity guard: refuse the operation rather than record it anonymously.
        throw new AccessDeniedException(
            "Operator identity could not be resolved from the authentication token. " +
            "Account operation aborted to preserve audit trail integrity."
        );
    }
}
