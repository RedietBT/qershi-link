package com.kab.qershi.account.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for opening a new member core account.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class OpenAccountRequest {

    @NotNull(message = "User ID is required.")
    private UUID userId;

    @NotBlank(message = "SACCO code is required.")
    @Size(max = 20, message = "SACCO code must not exceed 20 characters.")
    private String saccoCode;

    @NotBlank(message = "Branch code is required.")
    @Size(max = 20, message = "Branch code must not exceed 20 characters.")
    private String branchCode;

    @NotBlank(message = "Product code is required.")
    @Size(max = 10, message = "Product code must not exceed 10 characters.")
    private String productCode;

    public OpenAccountRequest() {}

    public OpenAccountRequest(UUID userId, String saccoCode, String branchCode, String productCode) {
        this.userId = userId;
        this.saccoCode = saccoCode;
        this.branchCode = branchCode;
        this.productCode = productCode;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
}
