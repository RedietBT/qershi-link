package com.kab.qershi.transaction.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request payload DTO for Member-to-Member internal transfer.
 */
public class TransferRequest {

    @NotBlank(message = "Sender account number is required.")
    private String senderAccountNo;

    @NotBlank(message = "Receiver account number is required.")
    private String receiverAccountNo;

    @NotNull(message = "Transfer amount is required.")
    @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01.")
    private BigDecimal amount;

    private String narration;

    public TransferRequest() {}

    public TransferRequest(String senderAccountNo, String receiverAccountNo, BigDecimal amount, String narration) {
        this.senderAccountNo = senderAccountNo;
        this.receiverAccountNo = receiverAccountNo;
        this.amount = amount;
        this.narration = narration;
    }

    public String getSenderAccountNo() { return senderAccountNo; }
    public void setSenderAccountNo(String senderAccountNo) { this.senderAccountNo = senderAccountNo; }

    public String getReceiverAccountNo() { return receiverAccountNo; }
    public void setReceiverAccountNo(String receiverAccountNo) { this.receiverAccountNo = receiverAccountNo; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNarration() { return narration; }
    public void setNarration(String narration) { this.narration = narration; }
}
