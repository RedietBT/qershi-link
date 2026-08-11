package com.kab.qershi.loan.origination.infrastructure.rest;

import com.kab.qershi.loan.origination.domain.model.LoanGroup;
import com.kab.qershi.loan.origination.domain.ports.inbound.GroupManagementUseCase;
import com.kab.qershi.loan.origination.domain.ports.inbound.GroupManagementUseCase.CreateGroupCommand;
import com.kab.qershi.loan.origination.infrastructure.rest.dto.CreateGroupRequest;
import com.kab.qershi.loan.origination.infrastructure.rest.dto.GroupResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller managing SACCO borrowing groups.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/loan-org/groups")
@Tag(name = "Borrowing Group Management", description = "Endpoints for registering and configuring SACCO borrowing groups")
public class GroupManagementController {

    private final GroupManagementUseCase groupManagementUseCase;

    public GroupManagementController(GroupManagementUseCase groupManagementUseCase) {
        this.groupManagementUseCase = groupManagementUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('LOAN_GROUP_MANAGE')")
    @Operation(summary = "Create Borrowing Group", description = "Onboards a formal or informal borrowing group with at least 2 members")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        CreateGroupCommand command = new CreateGroupCommand(
                request.groupName(),
                request.isFormal(),
                request.licenseNo(),
                request.memberUserIds(),
                request.leaderUserId()
        );

        LoanGroup group = groupManagementUseCase.createGroup(command);
        return new ResponseEntity<>(GroupResponse.fromDomain(group), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAnyAuthority('LOAN_GROUP_MANAGE', 'LOAN_APPLICATION_VIEW')")
    @Operation(summary = "Get Borrowing Group by ID", description = "Retrieves group roster and details by group UUID")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable("id") UUID groupId) {
        LoanGroup group = groupManagementUseCase.getGroupById(groupId);
        return ResponseEntity.ok(GroupResponse.fromDomain(group));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('LOAN_GROUP_MANAGE')")
    @Operation(summary = "List Borrowing Groups", description = "Lists all registered borrowing groups within active tenant schema")
    public ResponseEntity<List<GroupResponse>> listGroups() {
        List<GroupResponse> response = groupManagementUseCase.listGroups().stream()
                .map(GroupResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
