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

    public AccountController(AccountOpeningUseCase accountOpeningUseCase) {
        this.accountOpeningUseCase = accountOpeningUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('ACCOUNT_OPEN')")
    @Operation(summary = "Open New Member Account", description = "Opens a core ledger account for a member and generates an ISO Luhn account number (e.g. 0001-002-101-0001429).")
    public ResponseEntity<ApiResponse<Account>> openAccount(@Valid @RequestBody OpenAccountRequest request) {
        Account account = accountOpeningUseCase.openAccount(
                request.getUserId(),
                request.getSaccoCode(),
                request.getBranchCode(),
                request.getProductCode()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "Member account opened successfully. Status: PENDING_APPROVAL"));
    }

    @PutMapping("/{accountNo}/approve")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('ACCOUNT_APPROVE')")
    @Operation(summary = "Approve Account Opening (Four-Eye Checker)", description = "Activates a pending account via Four-Eye Maker-Checker approval workflow.")
    public ResponseEntity<ApiResponse<Account>> approveAccount(@PathVariable String accountNo, Authentication authentication) {
        UUID checkerUserId = parseUserId(authentication);
        Account approved = accountOpeningUseCase.approveAccount(accountNo, checkerUserId);
        return ResponseEntity.ok(ApiResponse.success(approved, "Account " + accountNo + " approved and activated successfully."));
    }

    @GetMapping("/{accountNo}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Get Account Details", description = "Retrieves account ledger balances and status by account number.")
    public ResponseEntity<ApiResponse<Account>> getAccountByNo(@PathVariable String accountNo) {
        Account account = accountOpeningUseCase.getAccountByNo(accountNo);
        return ResponseEntity.ok(ApiResponse.success(account, "Account details retrieved successfully."));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Get Member Accounts", description = "Retrieves all accounts owned by a specific member within the active SACCO tenant.")
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByUserId(@PathVariable UUID userId) {
        List<Account> accounts = accountOpeningUseCase.getAccountsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(accounts, "Retrieved " + accounts.size() + " accounts for member."));
    }

    @GetMapping("/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Find Accounts by Phone Number", description = "Tenant-isolated lookup returning accounts linked to member phone number in the active SACCO tenant.")
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByPhoneNumber(@PathVariable String phoneNumber) {
        List<Account> accounts = accountOpeningUseCase.getAccountsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(ApiResponse.success(accounts, "Found " + accounts.size() + " accounts linked to phone number " + phoneNumber + " in this SACCO."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or hasAuthority('ACCOUNT_VIEW_ALL')")
    @Operation(summary = "List All Accounts", description = "Retrieves all member accounts across the active SACCO tenant.")
    public ResponseEntity<ApiResponse<List<Account>>> getAllAccounts() {
        List<Account> accounts = accountOpeningUseCase.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts, "Retrieved " + accounts.size() + " accounts."));
    }

    private UUID parseUserId(Authentication auth) {
        if (auth == null) return UUID.randomUUID();
        Object principal = auth.getPrincipal();
        if (principal instanceof UUID) {
            return (UUID) principal;
        }
        try {
            return UUID.fromString(principal.toString());
        } catch (Exception ex) {
            return UUID.randomUUID();
        }
    }
}
