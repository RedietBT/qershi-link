package com.kab.qershi.common.event;

/**
 * Standard Apache Kafka Topic names across the Qershi-Link Core Banking Platform.
 * One topic per business domain, partitioned by saccoCode for tenant isolation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public final class BankingKafkaTopics {

    private BankingKafkaTopics() {}

    /**
     * Domain event topic for all transactional ledger activities (OTC deposits, withdrawals, transfers).
     */
    public static final String TRANSACTIONS = "banking.transactions";

    /**
     * Domain event topic for loan lifecycles (disbursements, repayments, defaults).
     */
    public static final String LOANS = "banking.loans";

    /**
     * Domain event topic for account lifecycles (account opened, interest capitalized, dormancy changes).
     */
    public static final String ACCOUNTS = "banking.accounts";
}
