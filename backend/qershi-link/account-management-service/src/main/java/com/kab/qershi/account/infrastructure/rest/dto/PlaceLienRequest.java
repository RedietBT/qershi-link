package com.kab.qershi.account.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for placing a monetary lien hold on an account.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class PlaceLienRequest {

    @NotNull(message = "Lien amount is required.")
    @DecimalMin(value = "0.01", message = "Lien amount must be greater than zero.")
    private BigDecimal amount;

    @NotBlank(message = "Reason is required.")
    private String reason;

    @Size(max = 100, message = "Reference number must not exceed 100 characters.")
    private String referenceNo;

    public PlaceLienRequest() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
