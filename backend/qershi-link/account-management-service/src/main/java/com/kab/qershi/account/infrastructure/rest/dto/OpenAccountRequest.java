package com.kab.qershi.account.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for opening a new member core account.
 * SACCO Code is automatically derived from the active SACCO tenant configuration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Schema(description = "Request payload for opening a new core member account")
public class OpenAccountRequest {

    @Schema(description = "Member user ID", example = "eff54b13-4a87-451a-8e19-5ae0d4efe075", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required.")
    private UUID userId;

    @Schema(description = "Branch code (Optional; defaults to SACCO configuration branch code '0001')", example = "0001")
    @Size(max = 20, message = "Branch code must not exceed 20 characters.")
    private String branchCode;

    @Schema(description = "Account product code (e.g. SAV01, SAVING)", example = "SAV01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product code is required.")
    @Size(max = 10, message = "Product code must not exceed 10 characters.")
    private String productCode;

    public OpenAccountRequest() {}

    public OpenAccountRequest(UUID userId, String branchCode, String productCode) {
        this.userId = userId;
        this.branchCode = branchCode;
        this.productCode = productCode;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
}
