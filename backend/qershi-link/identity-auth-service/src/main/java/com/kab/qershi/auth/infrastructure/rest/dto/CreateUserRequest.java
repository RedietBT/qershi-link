package com.kab.qershi.auth.infrastructure.rest.dto;

import com.kab.qershi.auth.domain.model.GlobalRole;

import java.util.UUID;

public record CreateUserRequest(
        String msisdn,
        String pin,
        GlobalRole globalRole,
        UUID saccoId
) {}
