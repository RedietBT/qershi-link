package com.kab.qershi.auth.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object representing an inbound request to onboard a new SACCO or Union.
 * Contains strict validation constraints to guarantee structural data sanity and input security.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Data
public class SaccoRegistrationRequest {

    @NotBlank(message = "SACCO name is a non-negotiable legal requirement and cannot be blank.")
    @Size(min = 3, max = 100, message = "SACCO legal title must be between 3 and 100 characters to prevent buffer issues.")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "SACCO name must only contain alphanumeric characters and spaces to block HTML/XSS scripts.")
    private String saccoName;

    @NotNull(message = "You must explicitly declare whether this entity is a Union (true) or a standard SACCO (false).")
    private Boolean isUnion;

    @NotNull(message = "Minimum share capital requirement must be specified.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Financial barriers to entry cannot drop below zero balance.")
    private BigDecimal minShareRequirement;

    @NotBlank(message = "An administrator mobile phone handle must be provided.")
    @Pattern(regexp = "^\\+251\\d{9}$", message = "Phone number must comply strictly with the E.164 standard formatting (+251XXXXXXXXX).")
    private String adminMsisdn;

    @NotBlank(message = "Administrator contact name is required.")
    @Size(min = 2, max = 60, message = "Administrator name must stay within a secure limit of 60 characters.")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Administrator name must contain only letters and single spaces to mitigate tag injections.")
    private String adminName;

    @NotBlank(message = "Operational regional domain allocation is required.")
    @Size(min = 2, max = 50, message = "Region name string length must be contained securely.")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Region name must strictly map to alphabetical character strings.")
    private String region;

    private Map<String, Object> metadata;
}