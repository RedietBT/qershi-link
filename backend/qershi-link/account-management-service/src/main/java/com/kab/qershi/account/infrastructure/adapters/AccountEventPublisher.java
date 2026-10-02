package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.ports.outbound.AccountEventPublisherPort;
import com.kab.qershi.common.event.AccountOpenedEvent;
import com.kab.qershi.common.event.BankingKafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Outbound Adapter implementing AccountEventPublisherPort.
 * Publishes events to Apache Kafka partitioned by saccoCode.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class AccountEventPublisher implements AccountEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(AccountEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AccountEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishAccountOpened(AccountOpenedEvent event) {
        if (event == null) return;
        String partitionKey = (event.getSaccoCode() != null && !event.getSaccoCode().isBlank())
                ? event.getSaccoCode()
                : "default";

        try {
            kafkaTemplate.send(BankingKafkaTopics.ACCOUNTS, partitionKey, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka account opened event published [Topic: {}, Partition: {}, Key: {}] for account: {}",
                                    BankingKafkaTopics.ACCOUNTS,
                                    result.getRecordMetadata().partition(),
                                    partitionKey,
                                    event.getAccountNo());
                        } else {
                            log.warn("Failed delivering account opened event for account {}: {}", event.getAccountNo(), ex.getMessage());
                        }
                    });
        } catch (Exception ex) {
            log.warn("Kafka event publish skipped (broker offline): {}", ex.getMessage());
        }
    }
}
