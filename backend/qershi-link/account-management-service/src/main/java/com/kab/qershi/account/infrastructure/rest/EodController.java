package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.application.usecase.EodBatchOrchestrator;
import com.kab.qershi.account.infrastructure.persistence.EodBatchExecutionEntity;
import com.kab.qershi.account.infrastructure.persistence.EodBatchStepLogEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataEodBatchExecutionRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataEodBatchStepLogRepository;
import com.kab.qershi.account.infrastructure.persistence.SystemBusinessDateEntity;
import com.kab.qershi.account.infrastructure.rest.dto.EodBatchHistoryResponse;
import com.kab.qershi.account.infrastructure.rest.dto.EodStatusResponse;
import com.kab.qershi.account.infrastructure.rest.dto.EodStepLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * REST API Controller for Core Banking End-of-Day (EOD) Operations.
 * Provides controls for viewing financial business dates, triggering manual batch runs,
 * and inspecting historical pipeline execution logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/eod")
@Tag(name = "Core Banking EOD Engine", description = "Endpoints for End-of-Day batch processing and business date control")
public class EodController {

    private final EodBatchOrchestrator orchestrator;
    private final SpringDataEodBatchExecutionRepository batchExecutionRepository;
    private final SpringDataEodBatchStepLogRepository stepLogRepository;

    public EodController(EodBatchOrchestrator orchestrator,
                         SpringDataEodBatchExecutionRepository batchExecutionRepository,
                         SpringDataEodBatchStepLogRepository stepLogRepository) {
        this.orchestrator = orchestrator;
        this.batchExecutionRepository = batchExecutionRepository;
        this.stepLogRepository = stepLogRepository;
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyAuthority('EOD_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get Business Date & EOD Status", description = "Inspect the current core banking business date and daytime operational state.")
    public ResponseEntity<EodStatusResponse> getEodStatus() {
        SystemBusinessDateEntity dateEntity = orchestrator.getOrCreateCurrentBusinessDate();
        EodBatchExecutionEntity lastBatch = batchExecutionRepository.findTopByOrderByStartedAtDesc().orElse(null);

        EodStatusResponse response = new EodStatusResponse(
                dateEntity.getCurrentBusinessDate(),
                dateEntity.getStatus(),
                dateEntity.getIsMonthEnd(),
                dateEntity.getLastEodCompletedAt(),
                lastBatch != null ? lastBatch.getBatchId() : null,
                lastBatch != null ? lastBatch.getStatus() : null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/run")
    @PreAuthorize("hasAnyAuthority('EOD_EXECUTE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Trigger Manual EOD Batch", description = "Executes the full End-of-Day batch pipeline: savings interest accrual, dormancy sweep, loan PAR aging, and business date rollover.")
    public ResponseEntity<EodBatchHistoryResponse> runEodBatch() {
        EodBatchExecutionEntity batch = orchestrator.runEodBatch("MANUAL_OVERRIDE", null);
        List<EodStepLogResponse> steps = getStepsForBatch(batch.getBatchId());

        return ResponseEntity.ok(mapToResponse(batch, steps));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('EOD_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "List EOD Batch History", description = "Retrieves all past End-of-Day batch execution logs ordered chronologically descending.")
    public ResponseEntity<List<EodBatchHistoryResponse>> getBatchHistory() {
        List<EodBatchExecutionEntity> history = batchExecutionRepository.findAllByOrderByStartedAtDesc();
        List<EodBatchHistoryResponse> response = history.stream()
                .map(b -> mapToResponse(b, Collections.emptyList()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{batchId}")
    @PreAuthorize("hasAnyAuthority('EOD_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get Batch Execution Details", description = "Fetches the full details and step-by-step pipeline execution logs for a specific batch run.")
    public ResponseEntity<EodBatchHistoryResponse> getBatchDetails(@PathVariable UUID batchId) {
        return batchExecutionRepository.findById(batchId)
                .map(batch -> {
                    List<EodStepLogResponse> steps = getStepsForBatch(batch.getBatchId());
                    return ResponseEntity.ok(mapToResponse(batch, steps));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private List<EodStepLogResponse> getStepsForBatch(UUID batchId) {
        List<EodBatchStepLogEntity> stepLogs = stepLogRepository.findByBatchIdOrderByCreatedAtAsc(batchId);
        return stepLogs.stream()
                .map(s -> new EodStepLogResponse(
                        s.getStepId(),
                        s.getStepName(),
                        s.getStatus(),
                        s.getDurationMs(),
                        s.getRecordsAffected(),
                        s.getErrorMessage(),
                        s.getCreatedAt()
                ))
                .toList();
    }

    private EodBatchHistoryResponse mapToResponse(EodBatchExecutionEntity b, List<EodStepLogResponse> steps) {
        return new EodBatchHistoryResponse(
                b.getBatchId(),
                b.getBusinessDate(),
                b.getStartedAt(),
                b.getCompletedAt(),
                b.getStatus(),
                b.getTriggeredBy(),
                b.getTotalAccountsAccrued(),
                b.getTotalInterestAccrued(),
                b.getTotalLoansEvaluated(),
                b.getTotalAccountsDormant(),
                b.getSummaryNotes(),
                steps
        );
    }
}
