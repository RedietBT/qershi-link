package com.kab.qershi.account.infrastructure.rest.dto;

import com.kab.qershi.account.domain.model.GlAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new custom General Ledger account in the Chart of Accounts.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class CreateChartOfAccountRequest {

    @NotBlank(message = "GL Code is required")
    @Size(min = 3, max = 50, message = "GL Code must be between 3 and 50 characters")
    private String glCode;

    @NotBlank(message = "Account name is required")
    @Size(min = 3, max = 150, message = "Account name must be between 3 and 150 characters")
    private String accountName;

    @NotNull(message = "Account type is required")
    private GlAccountType accountType;

    private String parentGlCode;

    private String description;

    private Boolean allowManualJournal = Boolean.TRUE;

    public CreateChartOfAccountRequest() {}

    public String getGlCode() {
        return glCode;
    }

    public void setGlCode(String glCode) {
        this.glCode = glCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public GlAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(GlAccountType accountType) {
        this.accountType = accountType;
    }

    public String getParentGlCode() {
        return parentGlCode;
    }

    public void setParentGlCode(String parentGlCode) {
        this.parentGlCode = parentGlCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAllowManualJournal() {
        return allowManualJournal;
    }

    public void setAllowManualJournal(Boolean allowManualJournal) {
        this.allowManualJournal = allowManualJournal;
    }
}
