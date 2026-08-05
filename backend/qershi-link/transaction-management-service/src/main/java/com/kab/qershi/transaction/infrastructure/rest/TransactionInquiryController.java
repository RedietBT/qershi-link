package com.kab.qershi.transaction.infrastructure.rest;

import com.kab.qershi.common.dto.ApiResponse;
import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.ports.inbound.TransactionInquiryUseCase;
import com.kab.qershi.transaction.infrastructure.rest.dto.JournalEntryResponse;
import com.kab.qershi.transaction.infrastructure.rest.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller exposing transaction statement lookups and General Ledger journal line audit endpoints.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "3. Transaction & GL Inquiries", description = "Endpoints for retrieving account transaction statements and General Ledger journal entry details.")
public class TransactionInquiryController {

    private static final Logger log = LoggerFactory.getLogger(TransactionInquiryController.class);
    private final TransactionInquiryUseCase transactionInquiryUseCase;

    public TransactionInquiryController(TransactionInquiryUseCase transactionInquiryUseCase) {
        this.transactionInquiryUseCase = transactionInquiryUseCase;
    }

    @GetMapping("/account/{accountNo}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('TRANSACTION_VIEW') or hasAuthority('USER_VIEW_ALL')")
    @Operation(summary = "Get Account Transaction History", description = "Retrieves complete chronological transaction history for a specific member account.")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAccountTransactions(
            @PathVariable("accountNo") String accountNo) {

        log.info("REST Get Account Transactions request for accountNo: {}", accountNo);
        List<Transaction> transactions = transactionInquiryUseCase.getAccountTransactions(accountNo);
        List<TransactionResponse> response = transactions.stream()
                .map(TransactionResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response, "Retrieved " + response.size() + " transactions for account " + accountNo));
    }

    @GetMapping("/{transactionRef}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('TRANSACTION_VIEW') or hasAuthority('USER_VIEW_ALL')")
    @Operation(summary = "Get Transaction Details by Reference", description = "Retrieves transaction details for a specific transaction reference.")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByRef(
            @PathVariable("transactionRef") String transactionRef) {

        log.info("REST Get Transaction by Ref request: {}", transactionRef);
        Transaction tx = transactionInquiryUseCase.getTransactionByRef(transactionRef);

        return ResponseEntity.ok(ApiResponse.success(TransactionResponse.fromDomain(tx), "Transaction retrieved successfully."));
    }

    @GetMapping("/{transactionRef}/journal")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('TRANSACTION_VIEW') or hasAuthority('REPORT_VIEW_ALL')")
    @Operation(summary = "Get General Ledger Journal Entry Details", description = "Retrieves General Ledger (GL) double-entry journal header and all Debit/Credit lines posted for a transaction reference.")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> getJournalEntryByRef(
            @PathVariable("transactionRef") String transactionRef) {

        log.info("REST Get GL Journal Entry for transactionRef: {}", transactionRef);
        JournalEntry journalEntry = transactionInquiryUseCase.getJournalEntryByTransactionRef(transactionRef);

        return ResponseEntity.ok(ApiResponse.success(JournalEntryResponse.fromDomain(journalEntry), "GL Journal entry retrieved successfully."));
    }
}
