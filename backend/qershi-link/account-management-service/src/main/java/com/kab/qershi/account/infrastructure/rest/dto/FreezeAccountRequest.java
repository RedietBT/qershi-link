package com.kab.qershi.account.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for administrative freeze/unfreeze controls.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class FreezeAccountRequest {

    @NotBlank(message = "Freeze status is required (NONE, DEBIT_FREEZE, CREDIT_FREEZE, FULL_FREEZE).")
    private String freezeStatus;

    private String reason;

    public FreezeAccountRequest() {}

    public String getFreezeStatus() { return freezeStatus; }
    public void setFreezeStatus(String freezeStatus) { this.freezeStatus = freezeStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
