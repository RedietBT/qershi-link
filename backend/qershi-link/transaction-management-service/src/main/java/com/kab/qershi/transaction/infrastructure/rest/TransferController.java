package com.kab.qershi.transaction.infrastructure.rest;

import com.kab.qershi.common.dto.ApiResponse;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.ports.inbound.TransferUseCase;
import com.kab.qershi.transaction.infrastructure.rest.dto.TransactionResponse;
import com.kab.qershi.transaction.infrastructure.rest.dto.TransferRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller exposing Member-to-Member internal fund transfer endpoints.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "2. Internal Transfer Operations", description = "Endpoints for processing member-to-member internal account transfers.")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferUseCase transferUseCase;

    public TransferController(TransferUseCase transferUseCase) {
        this.transferUseCase = transferUseCase;
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('TRANSACTION_TRANSFER')")
    @Operation(summary = "Process Member-to-Member Transfer", description = "Transfers funds between two member accounts within the same SACCO and posts balanced GL entries.")
    public ResponseEntity<ApiResponse<TransactionResponse>> processTransfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        UUID processedByUserId = extractCurrentUserId();
        log.info("REST Transfer Request: sender={}, receiver={}, amount={}, processedBy={}",
                request.getSenderAccountNo(), request.getReceiverAccountNo(), request.getAmount(), processedByUserId);

        Transaction tx = transferUseCase.processTransfer(
                request.getSenderAccountNo(),
                request.getReceiverAccountNo(),
                request.getAmount(),
                request.getNarration(),
                idempotencyKey,
                processedByUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(TransactionResponse.fromDomain(tx), "Internal member transfer processed successfully."));
    }

    private UUID extractCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getPrincipal() instanceof UUID uuid) {
                return uuid;
            }
            if (auth.getDetails() != null) {
                try {
                    return UUID.fromString(auth.getDetails().toString());
                } catch (Exception ignored) {}
            }
            try {
                return UUID.fromString(auth.getName());
            } catch (Exception ignored) {}
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
