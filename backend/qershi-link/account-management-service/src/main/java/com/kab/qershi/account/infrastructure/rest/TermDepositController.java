package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.application.usecase.TermDepositService;
import com.kab.qershi.account.infrastructure.persistence.TermDepositContractEntity;
import com.kab.qershi.account.infrastructure.persistence.TermDepositContractEntity.TermDepositStatus;
import com.kab.qershi.account.infrastructure.rest.dto.OpenTermDepositRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API Controller for Fixed Term Deposit (FD) Contract Lifecycle.
 * Implements Temenos Transact / Finacle FD management: open, mature, early-break.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/term-deposits")
@Tag(name = "Fixed Term Deposits (FD)", description = "Full lifecycle management of Fixed Term Deposit contracts with penalty-break engine")
public class TermDepositController {

    private final TermDepositService termDepositService;

    public TermDepositController(TermDepositService termDepositService) {
        this.termDepositService = termDepositService;
    }

    // ── 1. Open a new Term Deposit ────────────────────────────────────────────

    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER') or hasAnyAuthority('ACCOUNT_CREATE', 'TERM_DEPOSIT_MANAGE')")
    @Operation(summary = "Open Fixed Term Deposit",
               description = "Locks member funds as a Fixed Term Deposit at a preferential rate. Posts DEBIT GL 1010 / CREDIT GL 2060.")
    public ResponseEntity<Map<String, Object>> openTermDeposit(
            @Valid @RequestBody OpenTermDepositRequest req,
            Authentication authentication) {

        // Resolve maker user ID from JWT token
        UUID makerUserId = resolveUserId(authentication);

        TermDepositService.OpenFdResult result = termDepositService.openTermDeposit(
                req.accountNo(), makerUserId, resolveSaccoCode(authentication), resolveBranchCode(authentication),
                req.principalAmount(), req.tenorMonths(), req.agreedInterestRatePa(),
                req.earlyBreakPenaltyPct(), req.autoRollover(), req.rolloverTenorMonths(),
                makerUserId, req.makerNotes());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "contractId",     result.contractId(),
                "contractNo",     result.contractNo(),
                "status",         result.status(),
                "principalAmount", result.principal(),
                "ratePercent",    result.ratePercent(),
                "maturityDate",   result.maturityDate().toString(),
                "openingGlRef",   result.openingGlRef(),
                "glEntry", Map.of(
                        "debit",  "GL 1010 — Member Savings Account",
                        "credit", "GL 2060 — Term Deposit Liability",
                        "amount", result.principal()
                )
        ));
    }

    // ── 2. List all FD contracts for an account ───────────────────────────────

    @GetMapping("/account/{accountNo}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER') or hasAnyAuthority('ACCOUNT_VIEW', 'TERM_DEPOSIT_VIEW')")
    @Operation(summary = "List Term Deposits by Account",
               description = "Returns all Fixed Term Deposit contracts for a given savings account.")
    public ResponseEntity<List<Map<String, Object>>> getByAccount(@PathVariable String accountNo) {
        return ResponseEntity.ok(termDepositService.getContractsByAccount(accountNo)
                .stream().map(this::toMap).toList());
    }

    // ── 3. List all active FDs (for dashboard) ────────────────────────────────

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('TERM_DEPOSIT_VIEW')")
    @Operation(summary = "List All Active Term Deposits",
               description = "Returns all currently active FD contracts across the portfolio.")
    public ResponseEntity<List<Map<String, Object>>> getAllActive() {
        return ResponseEntity.ok(termDepositService.getAllActiveContracts()
                .stream().map(this::toMap).toList());
    }

    // ── 4. Get single contract ────────────────────────────────────────────────

    @GetMapping("/{contractId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER') or hasAnyAuthority('TERM_DEPOSIT_VIEW')")
    @Operation(summary = "Get Term Deposit Contract", description = "Returns full details of a specific FD contract.")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID contractId) {
        return termDepositService.getContractById(contractId)
                .map(c -> ResponseEntity.ok(toMap(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 5. Early Break / Premature Termination ────────────────────────────────

    @PostMapping("/{contractId}/break-early")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('TERM_DEPOSIT_MANAGE')")
    @Operation(summary = "Break Term Deposit Early",
               description = "Prematurely terminates an ACTIVE FD contract. Applies penalty formula and credits net payout to savings account. Requires supervisor (checker) authorization — four-eye rule enforced.")
    public ResponseEntity<Map<String, Object>> breakEarly(
            @PathVariable UUID contractId,
            @RequestParam(required = false) String checkerNotes,
            Authentication authentication) {

        UUID checkerUserId = resolveUserId(authentication);
        TermDepositService.EarlyBreakResult result =
                termDepositService.breakTermDepositEarly(contractId, checkerUserId, checkerNotes);

        return ResponseEntity.ok(Map.of(
                "contractId",       result.contractId(),
                "contractNo",       result.contractNo(),
                "principal",        result.principal(),
                "accruedInterest",  result.accruedInterest(),
                "penaltyAmount",    result.penaltyAmount(),
                "netPayoutAmount",  result.netPayoutAmount(),
                "closingGlRef",     result.closingGlRef(),
                "status",           "CLOSED_EARLY"
        ));
    }

    // ── 6. Manual maturity sweep trigger (EOD or admin override) ─────────────

    @PostMapping("/process-matured")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or hasAnyAuthority('TERM_DEPOSIT_MANAGE')")
    @Operation(summary = "Process Matured Term Deposits",
               description = "EOD sweep: closes or rolls over all FD contracts that have reached their maturity date.")
    public ResponseEntity<Map<String, Object>> processMatured(
            @RequestParam(required = false) String businessDate) {

        LocalDate date = (businessDate != null && !businessDate.isBlank())
                ? LocalDate.parse(businessDate)
                : LocalDate.now();

        TermDepositService.MaturityProcessResult result =
                termDepositService.processMaturedContracts(date);

        return ResponseEntity.ok(Map.of(
                "businessDate",       date.toString(),
                "contractsProcessed", result.contractsProcessed(),
                "closedNormal",       result.closedNormal(),
                "autoRolledOver",     result.autoRolledOver(),
                "totalInterestPaid",  result.totalInterestPaid()
        ));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Map<String, Object> toMap(TermDepositContractEntity c) {
        return Map.ofEntries(
                Map.entry("contractId",            c.getContractId()),
                Map.entry("contractNo",            c.getContractNo()),
                Map.entry("accountNo",             c.getAccountNo()),
                Map.entry("userId",                c.getUserId()),
                Map.entry("principalAmount",       c.getPrincipalAmount()),
                Map.entry("tenorMonths",           c.getTenorMonths()),
                Map.entry("agreedInterestRatePa",  c.getAgreedInterestRatePa()),
                Map.entry("earlyBreakPenaltyPct",  c.getEarlyBreakPenaltyPct()),
                Map.entry("startDate",             c.getStartDate().toString()),
                Map.entry("maturityDate",          c.getMaturityDate().toString()),
                Map.entry("accruedInterest",       c.getAccruedInterest()),
                Map.entry("status",                c.getStatus().name()),
                Map.entry("autoRollover",          c.getAutoRollover()),
                Map.entry("openingGlRef",          c.getOpeningGlRef() != null ? c.getOpeningGlRef() : ""),
                Map.entry("closingGlRef",          c.getClosingGlRef() != null ? c.getClosingGlRef() : ""),
                Map.entry("penaltyAmount",         c.getPenaltyAmount() != null ? c.getPenaltyAmount() : BigDecimal.ZERO),
                Map.entry("netPayoutAmount",       c.getNetPayoutAmount() != null ? c.getNetPayoutAmount() : BigDecimal.ZERO),
                Map.entry("createdAt",             c.getCreatedAt().toString())
        );
    }

    private UUID resolveUserId(Authentication auth) {
        if (auth == null) return UUID.randomUUID(); // fallback for unauthenticated test calls
        try {
            return UUID.fromString(auth.getName());
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    private String resolveSaccoCode(Authentication auth) {
        // In a real multi-tenant system this comes from the JWT claim; default to HEAD_OFFICE
        return "HEAD_OFFICE";
    }

    private String resolveBranchCode(Authentication auth) {
        return "001";
    }
}
