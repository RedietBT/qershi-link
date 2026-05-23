package com.kab.qershi.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Data Transfer Object capturing credentials submitted during a multi-tenant session connection request.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Authentication requires a target identity handle.")
    @Pattern(regexp = "^\\+251\\d{9}$", message = "Provided phone identifier does not match recognized operational patterns (+251XXXXXXXXX).")
    private String msisdn;

    @NotBlank(message = "Verification pin or password credential is mandatory.")
    @Pattern(regexp = "^\\d{4,6}$", message = "Security security boundaries require a numeric passcode string between 4 to 6 characters.")
    private String pin;
}