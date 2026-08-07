package com.kab.qershi.loan.origination.application.usecase;

import com.kab.qershi.loan.origination.domain.model.*;
import com.kab.qershi.loan.origination.domain.ports.inbound.LoanApplicationUseCase;
import com.kab.qershi.loan.origination.domain.ports.outbound.LoanApplicationRepositoryPort;
import com.kab.qershi.loan.origination.domain.ports.outbound.LoanGroupRepositoryPort;
import com.kab.qershi.loan.origination.domain.service.ScoringEngine;
import com.kab.qershi.loan.origination.infrastructure.config.TenantContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service implementing LoanApplicationUseCase.
 * Handles loan application creation, collateral checks, automated multi-factor credit scoring,
 * and application reference number generation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class OriginationService implements LoanApplicationUseCase {

    private static final Logger log = LoggerFactory.getLogger(OriginationService.class);

    private final LoanApplicationRepositoryPort applicationRepositoryPort;
    private final LoanGroupRepositoryPort groupRepositoryPort;
    private final ScoringEngine scoringEngine;
    private final JdbcTemplate jdbcTemplate;

    public OriginationService(LoanApplicationRepositoryPort applicationRepositoryPort,
                              LoanGroupRepositoryPort groupRepositoryPort,
                              ScoringEngine scoringEngine,
                              JdbcTemplate jdbcTemplate) {
        this.applicationRepositoryPort = applicationRepositoryPort;
        this.groupRepositoryPort = groupRepositoryPort;
        this.scoringEngine = scoringEngine;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public LoanApplication submitApplication(SubmitApplicationCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("Borrower userId cannot be null.");
        }
        if (command.productId() == null) {
            throw new IllegalArgumentException("Loan productId cannot be null.");
        }
        if (command.amountRequested() == null || command.amountRequested().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount requested must be greater than zero.");
        }

        if (command.groupId() != null) {
            groupRepositoryPort.findById(command.groupId())
                    .orElseThrow(() -> new IllegalArgumentException("Specified borrowing group not found: " + command.groupId()));
        }

        String scoringType = command.scoringType() != null ? command.scoringType().toUpperCase().trim() : "SAVINGS";

        List<Collateral> collaterals = new ArrayList<>();
        if (command.collaterals() != null) {
            for (CollateralInput input : command.collaterals()) {
                collaterals.add(new Collateral(
                        UUID.randomUUID(),
                        null,
                        input.type(),
                        input.estimatedValue(),
                        input.documentUrl(),
                        Instant.now()
                ));
            }
        }

        if ("COLLATERAL".equalsIgnoreCase(scoringType) && collaterals.isEmpty()) {
            throw new IllegalArgumentException("At least one collateral asset must be pledged when using COLLATERAL credit scoring.");
        }

        UUID applicationId = UUID.randomUUID();
        String applicationNo = generateApplicationNumber();

        CreditScoring scoring = scoringEngine.evaluateEligibility(
                applicationId,
                scoringType,
                command.amountRequested(),
                command.savingsConsistency(),
                command.historicalYield(),
                command.projectedYield(),
                command.landSizeHectares(),
                collaterals
        );

        ApplicationStatus initialStatus = scoring.isPassedEligibility()
                ? ApplicationStatus.UNDER_REVIEW
                : ApplicationStatus.REJECTED_ELIGIBILITY;

        ApprovalLog submitLog = new ApprovalLog(
                UUID.randomUUID(),
                applicationId,
                command.userId(),
                WorkflowAction.SUBMIT,
                "Loan application submitted into origination workflow.",
                Instant.now()
        );

        LoanApplication application = new LoanApplication(
                applicationId,
                applicationNo,
                command.userId(),
                command.groupId(),
                command.productId(),
                scoringType,
                command.amountRequested(),
                null,
                initialStatus,
                scoring,
                collaterals,
                List.of(submitLog),
                Instant.now(),
                Instant.now()
        );

        LoanApplication savedApp = applicationRepositoryPort.save(application);
        log.info("Successfully submitted loan application {} (Status: {}, Passed Pre-Eligibility: {})",
                savedApp.getApplicationNo(), savedApp.getStatus(), scoring.isPassedEligibility());

        return savedApp;
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplication getApplicationById(UUID applicationId) {
        return applicationRepositoryPort.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + applicationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplication> listApplicationsForUser(UUID userId) {
        return applicationRepositoryPort.findByUserId(userId);
    }

    private String generateApplicationNumber() {
        String schema = TenantContext.getTenantSchema();
        String saccoCode = "SACCO";

        if (schema != null && !schema.isBlank()) {
            try {
                String sql = "SELECT sacco_code FROM master_schema.sacco_registry WHERE schema_name = ?";
                String fetchedCode = jdbcTemplate.queryForObject(sql, String.class, schema);
                if (fetchedCode != null && !fetchedCode.isBlank()) {
                    saccoCode = fetchedCode.toUpperCase().trim();
                }
            } catch (Exception ex) {
                log.trace("Could not query sacco_code for schema {}: {}", schema, ex.getMessage());
            }
        }

        int year = Year.now().getValue();
        long sequence = applicationRepositoryPort.countApplicationsCreatedInCurrentYear() + 1;
        return String.format("APP-%s-%d-%06d", saccoCode, year, sequence);
    }
}
