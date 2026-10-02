package com.kab.qershi.transaction.infrastructure.adapters;

import com.kab.qershi.common.event.BankingKafkaTopics;
import com.kab.qershi.common.event.TransactionCompletedEvent;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Outbound Adapter implementing TransactionEventPublisherPort.
 * Publishes events to Apache Kafka partitioned by saccoCode.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TransactionEventPublisher implements TransactionEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        if (event == null) return;

        String partitionKey = (event.getSaccoCode() != null && !event.getSaccoCode().isBlank())
                ? event.getSaccoCode()
                : "default";

        try {
            kafkaTemplate.send(BankingKafkaTopics.TRANSACTIONS, partitionKey, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka event published [Topic: {}, Partition: {}, Key: {}] for tx: {}",
                                    BankingKafkaTopics.TRANSACTIONS,
                                    result.getRecordMetadata().partition(),
                                    partitionKey,
                                    event.getTransactionId());
                        } else {
                            log.warn("Failed delivering Kafka event for tx {}: {}", event.getTransactionId(), ex.getMessage());
                        }
                    });
        } catch (Exception ex) {
            log.warn("Kafka event publish skipped (broker offline or unreachable): {}", ex.getMessage());
        }
    }
}
