package com.kab.qershi.loan.management.infrastructure.adapters;

import com.kab.qershi.common.event.BankingKafkaTopics;
import com.kab.qershi.common.event.LoanDisbursedEvent;
import com.kab.qershi.common.event.RepaymentReceivedEvent;
import com.kab.qershi.loan.management.domain.port.out.LoanEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Outbound Adapter implementing LoanEventPublisherPort.
 * Publishes events to Apache Kafka partitioned by saccoCode.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanEventPublisher implements LoanEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LoanEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LoanEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishLoanDisbursed(LoanDisbursedEvent event) {
        if (event == null) return;
        String partitionKey = (event.getSaccoCode() != null && !event.getSaccoCode().isBlank())
                ? event.getSaccoCode()
                : "default";

        try {
            kafkaTemplate.send(BankingKafkaTopics.LOANS, partitionKey, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka loan disbursed event published [Topic: {}, Partition: {}, Key: {}] for loan: {}",
                                    BankingKafkaTopics.LOANS,
                                    result.getRecordMetadata().partition(),
                                    partitionKey,
                                    event.getLoanId());
                        } else {
                            log.warn("Failed delivering loan disbursed event for loan {}: {}", event.getLoanId(), ex.getMessage());
                        }
                    });
        } catch (Exception ex) {
            log.warn("Kafka event publish skipped (broker offline): {}", ex.getMessage());
        }
    }

    @Override
    public void publishRepaymentReceived(RepaymentReceivedEvent event) {
        if (event == null) return;
        String partitionKey = (event.getSaccoCode() != null && !event.getSaccoCode().isBlank())
                ? event.getSaccoCode()
                : "default";

        try {
            kafkaTemplate.send(BankingKafkaTopics.LOANS, partitionKey, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka repayment received event published [Topic: {}, Partition: {}, Key: {}] for loan: {}",
                                    BankingKafkaTopics.LOANS,
                                    result.getRecordMetadata().partition(),
                                    partitionKey,
                                    event.getLoanId());
                        } else {
                            log.warn("Failed delivering repayment event for loan {}: {}", event.getLoanId(), ex.getMessage());
                        }
                    });
        } catch (Exception ex) {
            log.warn("Kafka event publish skipped (broker offline): {}", ex.getMessage());
        }
    }
}
