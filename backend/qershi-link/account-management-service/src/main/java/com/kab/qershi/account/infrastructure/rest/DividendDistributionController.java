package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.domain.model.DividendAllocation;
import com.kab.qershi.account.domain.model.DividendDistribution;
import com.kab.qershi.account.domain.ports.inbound.DividendDistributionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API Controller — Annual AGM Dividend Distribution Engine.
 *
 * Every endpoint guards by BOTH role AND authority (permission) so that
 * fine-grained permission assignments work independently of role membership.
 *
 * Workflow:
 *  1. POST /simulate  → dry-run; saves SIMULATED status — requires DIVIDEND_SIMULATE
 *  2. POST /{id}/post → batch GL credits — requires DIVIDEND_POST (promoted privilege)
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/dividends")
@Tag(name = "AGM Dividend Distribution", description = "Annual dividend: weighted-share calculation, 5% WHT, batch credit to member savings accounts")
public class DividendDistributionController {

    private final DividendDistributionUseCase dividendUseCase;

    public DividendDistributionController(DividendDistributionUseCase dividendUseCase) {
        this.dividendUseCase = dividendUseCase;
    }

    // ── 1. Simulate Distribution ──────────────────────────────────────────────

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN') or hasAnyAuthority('DIVIDEND_SIMULATE')")
    @Operation(summary = "Simulate AGM dividend distribution (dry-run)",
               description = "Calculates per-member dividend allocation. Does NOT post GL entries. Re-running the same fiscal year re-simulates.")
    public ResponseEntity<DividendDistributionUseCase.SimulationResult> simulate(
            @RequestBody Map<String, Object> body) {
        int fiscalYear = ((Number) body.get("fiscalYear")).intValue();
        BigDecimal netProfitPool = new BigDecimal(body.get("netProfitPool").toString());
        BigDecimal declaredRatePercent = new BigDecimal(body.get("declaredRatePercent").toString());
        return ResponseEntity.ok(dividendUseCase.simulate(fiscalYear, netProfitPool, declaredRatePercent));
    }

    // ── 2. Post (Batch Execute) ───────────────────────────────────────────────

    @PostMapping("/{distributionId}/post")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SACCO_ADMIN') or hasAnyAuthority('DIVIDEND_POST')")
    @Operation(summary = "Post dividend distribution — batch-credits net payout to member savings accounts")
    public ResponseEntity<DividendDistributionUseCase.PostingResult> post(
            @PathVariable UUID distributionId,
            Authentication authentication) {
        UUID postedBy = extractUserId(authentication);
        return ResponseEntity.ok(dividendUseCase.post(distributionId, postedBy));
    }

    // ── 3. List All Distributions ─────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','AUDITOR') or hasAnyAuthority('DIVIDEND_VIEW','FINANCIAL_REPORT_VIEW')")
    @Operation(summary = "List all dividend distribution headers across fiscal years")
    public ResponseEntity<List<DividendDistribution>> getAllDistributions() {
        return ResponseEntity.ok(dividendUseCase.getAllDistributions());
    }

    // ── 4. Get by Fiscal Year ─────────────────────────────────────────────────

    @GetMapping("/year/{fiscalYear}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','AUDITOR') or hasAnyAuthority('DIVIDEND_VIEW','FINANCIAL_REPORT_VIEW')")
    @Operation(summary = "Get distribution header for a specific fiscal year")
    public ResponseEntity<DividendDistribution> getByFiscalYear(@PathVariable int fiscalYear) {
        return ResponseEntity.ok(dividendUseCase.getByFiscalYear(fiscalYear));
    }

    // ── 5. Get Allocations for a Distribution ────────────────────────────────

    @GetMapping("/{distributionId}/allocations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','AUDITOR') or hasAnyAuthority('DIVIDEND_VIEW','FINANCIAL_REPORT_VIEW')")
    @Operation(summary = "Get per-member allocation lines for a distribution run")
    public ResponseEntity<List<DividendAllocation>> getAllocations(@PathVariable UUID distributionId) {
        return ResponseEntity.ok(dividendUseCase.getAllocations(distributionId));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        try {
            if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                try { return UUID.fromString(ud.getUsername()); } catch (Exception ex) { return null; }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
