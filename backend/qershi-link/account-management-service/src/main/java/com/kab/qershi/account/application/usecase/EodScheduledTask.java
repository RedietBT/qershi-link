package com.kab.qershi.account.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Automated Midnight Scheduler for Core Banking End-of-Day (EOD) Batch Processing.
 * Triggers batch runner automatically unless disabled in configuration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
@ConditionalOnProperty(name = "core.banking.eod.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class EodScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(EodScheduledTask.class);

    private final EodBatchOrchestrator orchestrator;

    public EodScheduledTask(EodBatchOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    // Default: Every midnight at 00:00:00 server time
    @Scheduled(cron = "${core.banking.eod.cron:0 0 0 * * ?}")
    public void executeMidnightEodBatch() {
        log.info("Scheduled Midnight Cron triggered for Core Banking EOD batch execution.");
        try {
            orchestrator.runEodBatch("SYSTEM_CRON", null);
            log.info("Scheduled Midnight EOD Batch execution completed successfully.");
        } catch (Exception e) {
            log.error("Scheduled Midnight EOD Batch failed: {}", e.getMessage(), e);
        }
    }
}
