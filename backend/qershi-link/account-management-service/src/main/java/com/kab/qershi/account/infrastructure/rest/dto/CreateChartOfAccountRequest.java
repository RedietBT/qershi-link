package com.kab.qershi.account.infrastructure.rest.dto;

import com.kab.qershi.account.domain.model.GlAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Validated and sanitized request payload for creating a new custom General Ledger account.
 * Enforces strict input validation to prevent injection, control characters, and malformed codes.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
public class CreateChartOfAccountRequest {

    @NotBlank(message = "GL Code is required")
    @Size(min = 3, max = 50, message = "GL Code must be between 3 and 50 characters")
    @Pattern(
            regexp = "^[0-9]{3,10}(-[0-9]{1,6})*$",
            message = "GL Code must consist of numeric digits optionally grouped with hyphens (e.g. '1010', '1010-001')"
    )
    private String glCode;

    @NotBlank(message = "Account name is required")
    @Size(min = 3, max = 150, message = "Account name must be between 3 and 150 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9\\s&/\\-\\(\\)]{3,150}$",
            message = "Account name contains invalid characters. Only alphanumeric, spaces, and safe punctuation (&, /, -, ()) are allowed."
    )
    private String accountName;

    @NotNull(message = "Account type is required (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE)")
    private GlAccountType accountType;

    @Pattern(
            regexp = "^$|^[0-9]{3,10}(-[0-9]{1,6})*$",
            message = "Parent GL Code must be numeric digits optionally grouped with hyphens"
    )
    private String parentGlCode;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9\\s\\.,\\-_/\\(\\)#]*$",
            message = "Description contains prohibited characters or potential script injection"
    )
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
