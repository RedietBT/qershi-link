package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.domain.model.StandingOrder;
import com.kab.qershi.account.domain.ports.inbound.StandingOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API Controller — Standing Orders (Automated Recurring Sweeps).
 *
 * Every endpoint guards by BOTH role AND authority (permission) so that
 * fine-grained permission assignments work independently of role membership.
 *
 * STANDING_ORDER_SWEEP authority is required for the manual sweep trigger —
 * a distinct promoted privilege separate from general management.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/standing-orders")
@Tag(name = "Standing Orders", description = "Automated recurring sweeps — create, pause, resume, cancel, and trigger daily sweep runner")
public class StandingOrderController {

    private final StandingOrderUseCase standingOrderUseCase;

    public StandingOrderController(StandingOrderUseCase standingOrderUseCase) {
        this.standingOrderUseCase = standingOrderUseCase;
    }

    // ── 1. Create ─────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER') or hasAnyAuthority('STANDING_ORDER_MANAGE','ACCOUNT_CREATE')")
    @Operation(summary = "Create standing order — validates source and target account status")
    public ResponseEntity<StandingOrder> create(@RequestBody Map<String, Object> body) {
        UUID memberId        = UUID.fromString((String) body.get("memberId"));
        String sourceAccountNo = (String) body.get("sourceAccountNo");
        String targetAccountNo = (String) body.get("targetAccountNo");
        BigDecimal amount    = new BigDecimal(body.get("amount").toString());
        String frequency     = (String) body.getOrDefault("frequency", "MONTHLY");
        Integer dayOfMonth   = body.containsKey("dayOfMonth") ? ((Number) body.get("dayOfMonth")).intValue() : null;
        String dayOfWeek     = (String) body.get("dayOfWeek");
        LocalDate startDate  = body.containsKey("startDate") ? LocalDate.parse((String) body.get("startDate")) : LocalDate.now();
        LocalDate endDate    = body.containsKey("endDate") ? LocalDate.parse((String) body.get("endDate")) : null;
        String description   = (String) body.get("description");

        var request = new StandingOrderUseCase.CreateStandingOrderRequest(
                memberId, sourceAccountNo, targetAccountNo,
                amount, frequency, dayOfMonth, dayOfWeek,
                startDate, endDate, description
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(standingOrderUseCase.create(request));
    }

    // ── 2. List All ───────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','AUDITOR') or hasAnyAuthority('STANDING_ORDER_VIEW','ACCOUNT_VIEW')")
    @Operation(summary = "List all standing orders platform-wide")
    public ResponseEntity<List<StandingOrder>> getAll() {
        return ResponseEntity.ok(standingOrderUseCase.getAll());
    }

    // ── 3. List by Member ─────────────────────────────────────────────────────

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER','AUDITOR') or hasAnyAuthority('STANDING_ORDER_VIEW','ACCOUNT_VIEW')")
    @Operation(summary = "Get standing orders for a specific member")
    public ResponseEntity<List<StandingOrder>> getByMember(@PathVariable UUID memberId) {
        return ResponseEntity.ok(standingOrderUseCase.getByMemberId(memberId));
    }

    // ── 4. Lookup by Phone Number ─────────────────────────────────────────────

    @GetMapping("/lookup/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER') or hasAnyAuthority('STANDING_ORDER_VIEW','ACCOUNT_VIEW')")
    @Operation(summary = "Lookup standing orders by member phone number",
               description = "Resolves the member account by phone, then returns all their standing orders.")
    public ResponseEntity<List<StandingOrder>> getByPhone(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(standingOrderUseCase.getByPhoneNumber(phoneNumber));
    }

    // ── 5. Get by ID ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER','AUDITOR') or hasAnyAuthority('STANDING_ORDER_VIEW','ACCOUNT_VIEW')")
    @Operation(summary = "Get standing order details by ID")
    public ResponseEntity<StandingOrder> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(standingOrderUseCase.getById(id));
    }

    // ── 6. Pause ──────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER') or hasAnyAuthority('STANDING_ORDER_MANAGE')")
    @Operation(summary = "Pause an active standing order")
    public ResponseEntity<StandingOrder> pause(@PathVariable UUID id) {
        return ResponseEntity.ok(standingOrderUseCase.pause(id));
    }

    // ── 7. Resume ─────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/resume")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER') or hasAnyAuthority('STANDING_ORDER_MANAGE')")
    @Operation(summary = "Resume a paused standing order")
    public ResponseEntity<StandingOrder> resume(@PathVariable UUID id) {
        return ResponseEntity.ok(standingOrderUseCase.resume(id));
    }

    // ── 8. Cancel ─────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN') or hasAnyAuthority('STANDING_ORDER_MANAGE')")
    @Operation(summary = "Cancel a standing order permanently")
    public ResponseEntity<StandingOrder> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(standingOrderUseCase.cancel(id));
    }

    // ── 9. Manual Sweep Trigger ───────────────────────────────────────────────

    @PostMapping("/run-sweeps")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SACCO_ADMIN') or hasAnyAuthority('STANDING_ORDER_SWEEP','EOD_EXECUTE')")
    @Operation(summary = "Manually trigger daily standing order sweeps",
               description = "Executes all due standing orders for a given date. Restricted to STANDING_ORDER_SWEEP or EOD_EXECUTE authority.")
    public ResponseEntity<StandingOrderUseCase.SweepRunResult> runSweeps(
            @RequestParam(required = false) String runDate) {
        LocalDate date = (runDate != null && !runDate.isBlank())
                ? LocalDate.parse(runDate) : LocalDate.now();
        return ResponseEntity.ok(standingOrderUseCase.runDailySweeps(date));
    }
}
