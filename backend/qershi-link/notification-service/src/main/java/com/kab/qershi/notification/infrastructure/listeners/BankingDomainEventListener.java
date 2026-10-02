package com.kab.qershi.notification.infrastructure.listeners;

import com.kab.qershi.common.event.AccountOpenedEvent;
import com.kab.qershi.common.event.BankingKafkaTopics;
import com.kab.qershi.common.event.LoanDisbursedEvent;
import com.kab.qershi.common.event.RepaymentReceivedEvent;
import com.kab.qershi.common.event.TransactionCompletedEvent;
import com.kab.qershi.notification.domain.model.NotificationChannel;
import com.kab.qershi.notification.domain.model.NotificationLanguage;
import com.kab.qershi.notification.domain.model.NotificationRequest;
import com.kab.qershi.notification.domain.ports.inbound.SendNotificationUseCase;
import com.kab.qershi.notification.infrastructure.config.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Asynchronous Kafka Event Consumer for Core Banking Domain Events.
 * Dispatches automated member SMS alerts based on transactional, loan, and account lifecycles.
 * Maintains tenant context dynamically from the event's saccoCode partition key.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class BankingDomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(BankingDomainEventListener.class);

    private final SendNotificationUseCase sendNotificationUseCase;

    public BankingDomainEventListener(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    /**
     * Consumes TransactionCompletedEvents from 'banking.transactions'.
     */
    @KafkaListener(topics = BankingKafkaTopics.TRANSACTIONS, groupId = "qershi-notification-transactions")
    public void onTransactionCompleted(TransactionCompletedEvent event) {
        if (event == null || event.getRecipientPhone() == null || event.getRecipientPhone().isBlank()) {
            return;
        }

        log.info("Received Kafka TransactionCompletedEvent [Type: {}, Account: {}, TxRef: {}]",
                event.getTransactionType(), event.getAccountNo(), event.getTransactionId());

        try {
            if (event.getSaccoCode() != null && !event.getSaccoCode().isBlank()) {
                TenantContext.setTenantSchema(event.getSaccoCode().trim());
            }

            String templateCode;
            Map<String, String> params = new HashMap<>();
            params.put("memberName", event.getMemberName() != null ? event.getMemberName() : "Member");
            params.put("amount", event.getAmount() != null ? event.getAmount().toPlainString() : "0.00");
            params.put("balance", event.getBalance() != null ? event.getBalance().toPlainString() : "0.00");
            params.put("accountNo", event.getAccountNo() != null ? event.getAccountNo() : "");

            if ("WITHDRAWAL".equalsIgnoreCase(event.getTransactionType())) {
                templateCode = "CASH_WITHDRAWAL_ALERT";
            } else if ("TRANSFER".equalsIgnoreCase(event.getTransactionType())) {
                templateCode = "TRANSFER_SENT_ALERT";
                params.put("receiverName", event.getReceiverName() != null ? event.getReceiverName() : "Recipient");
                params.put("receiverAccountNo", event.getReceiverAccountNo() != null ? event.getReceiverAccountNo() : "");
            } else {
                templateCode = "CASH_DEPOSIT_ALERT";
            }

            NotificationRequest request = new NotificationRequest(
                    event.getRecipientPhone(),
                    templateCode,
                    null,
                    params,
                    NotificationChannel.SMS,
                    NotificationLanguage.EN,
                    null
            );

            sendNotificationUseCase.sendTemplatedNotification(request);
        } catch (Exception ex) {
            log.error("Error processing Kafka transaction event {}: {}", event.getTransactionId(), ex.getMessage(), ex);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Consumes LoanDisbursedEvents from 'banking.loans'.
     */
    @KafkaListener(topics = BankingKafkaTopics.LOANS, groupId = "qershi-notification-loans")
    public void onLoanEvent(Object eventObj) {
        if (eventObj instanceof LoanDisbursedEvent event) {
            handleLoanDisbursed(event);
        } else if (eventObj instanceof RepaymentReceivedEvent event) {
            handleRepaymentReceived(event);
        } else {
            log.debug("Received generic loan event: {}", eventObj != null ? eventObj.getClass().getSimpleName() : "null");
        }
    }

    private void handleLoanDisbursed(LoanDisbursedEvent event) {
        if (event == null || event.getRecipientPhone() == null || event.getRecipientPhone().isBlank()) return;

        log.info("Received Kafka LoanDisbursedEvent [LoanId: {}, Amount: {}]", event.getLoanId(), event.getAmount());

        try {
            if (event.getSaccoCode() != null && !event.getSaccoCode().isBlank()) {
                TenantContext.setTenantSchema(event.getSaccoCode().trim());
            }

            Map<String, String> params = new HashMap<>();
            params.put("memberName", event.getMemberName() != null ? event.getMemberName() : "Member");
            params.put("amount", event.getAmount() != null ? event.getAmount().toPlainString() : "0.00");
            params.put("accountNo", event.getAccountNo() != null ? event.getAccountNo() : "");

            NotificationRequest request = new NotificationRequest(
                    event.getRecipientPhone(),
                    "LOAN_DISBURSED",
                    null,
                    params,
                    NotificationChannel.SMS,
                    NotificationLanguage.EN,
                    null
            );

            sendNotificationUseCase.sendTemplatedNotification(request);
        } catch (Exception ex) {
            log.error("Error processing loan disbursed event {}: {}", event.getLoanId(), ex.getMessage(), ex);
        } finally {
            TenantContext.clear();
        }
    }

    private void handleRepaymentReceived(RepaymentReceivedEvent event) {
        if (event == null || event.getRecipientPhone() == null || event.getRecipientPhone().isBlank()) return;

        log.info("Received Kafka RepaymentReceivedEvent [LoanId: {}, Amount: {}]", event.getLoanId(), event.getAmount());

        try {
            if (event.getSaccoCode() != null && !event.getSaccoCode().isBlank()) {
                TenantContext.setTenantSchema(event.getSaccoCode().trim());
            }

            Map<String, String> params = new HashMap<>();
            params.put("memberName", event.getMemberName() != null ? event.getMemberName() : "Member");
            params.put("amount", event.getAmount() != null ? event.getAmount().toPlainString() : "0.00");
            params.put("loanId", event.getLoanId() != null ? event.getLoanId() : "");
            params.put("remainingBalance", event.getRemainingBalance() != null ? event.getRemainingBalance().toPlainString() : "0.00");

            NotificationRequest request = new NotificationRequest(
                    event.getRecipientPhone(),
                    "LOAN_REPAYMENT_CONFIRMATION",
                    null,
                    params,
                    NotificationChannel.SMS,
                    NotificationLanguage.EN,
                    null
            );

            sendNotificationUseCase.sendTemplatedNotification(request);
        } catch (Exception ex) {
            log.error("Error processing repayment received event {}: {}", event.getLoanId(), ex.getMessage(), ex);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Consumes AccountOpenedEvents from 'banking.accounts'.
     */
    @KafkaListener(topics = BankingKafkaTopics.ACCOUNTS, groupId = "qershi-notification-accounts")
    public void onAccountOpened(AccountOpenedEvent event) {
        if (event == null || event.getRecipientPhone() == null || event.getRecipientPhone().isBlank()) return;

        log.info("Received Kafka AccountOpenedEvent [AccountNo: {}, Product: {}]", event.getAccountNo(), event.getProductName());

        try {
            if (event.getSaccoCode() != null && !event.getSaccoCode().isBlank()) {
                TenantContext.setTenantSchema(event.getSaccoCode().trim());
            }

            Map<String, String> params = new HashMap<>();
            params.put("memberName", event.getMemberName() != null ? event.getMemberName() : "Member");
            params.put("accountNo", event.getAccountNo() != null ? event.getAccountNo() : "");
            params.put("productName", event.getProductName() != null ? event.getProductName() : "Savings");

            NotificationRequest request = new NotificationRequest(
                    event.getRecipientPhone(),
                    "ACCOUNT_OPENED_ALERT",
                    null,
                    params,
                    NotificationChannel.SMS,
                    NotificationLanguage.EN,
                    null
            );

            sendNotificationUseCase.sendTemplatedNotification(request);
        } catch (Exception ex) {
            log.error("Error processing account opened event {}: {}", event.getAccountNo(), ex.getMessage(), ex);
        } finally {
            TenantContext.clear();
        }
    }
}
