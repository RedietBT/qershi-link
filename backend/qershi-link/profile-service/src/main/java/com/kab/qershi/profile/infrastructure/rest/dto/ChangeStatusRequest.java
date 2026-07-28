package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.domain.model.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * REST API Request DTO for changing member lifecycle status.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {

    @NotNull(message = "New status is required")
    private MemberStatus status;

    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
}
