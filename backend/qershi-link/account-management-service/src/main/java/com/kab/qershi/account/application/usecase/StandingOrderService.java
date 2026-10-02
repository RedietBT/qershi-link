package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.domain.model.StandingOrder;
import com.kab.qershi.account.domain.ports.inbound.StandingOrderUseCase;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.StandingOrderRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Hexagonal Application Service — Automated Recurring Standing Orders (Sweep Engine).
 *
 * Sweep Execution Logic:
 *  1. Load all ACTIVE standing orders where nextRunDate <= today
 *  2. For each order: verify source balance → debit source → credit target → advance nextRunDate
 *  3. On insufficient funds: record failure, increment failedAttemptsCount
 *  4. After 3 consecutive failures the order auto-transitions to FAILED
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class StandingOrderService implements StandingOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(StandingOrderService.class);

    private final StandingOrderRepositoryPort standingOrderRepository;
    private final AccountRepositoryPort accountRepository;

    public StandingOrderService(
            StandingOrderRepositoryPort standingOrderRepository,
            AccountRepositoryPort accountRepository) {
        this.standingOrderRepository = standingOrderRepository;
        this.accountRepository = accountRepository;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandingOrder create(CreateStandingOrderRequest req) {
        // Validate source account
        Account sourceAccount = accountRepository.findByAccountNo(req.sourceAccountNo())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + req.sourceAccountNo()));
        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not ACTIVE.");
        }

        // Validate target account
        Account targetAccount = accountRepository.findByAccountNo(req.targetAccountNo())
                .orElseThrow(() -> new IllegalArgumentException("Target account not found: " + req.targetAccountNo()));
        if (targetAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Target account is not ACTIVE.");
        }

        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Standing order amount must be greater than zero.");
        }

        LocalDate startDate    = req.startDate() != null ? req.startDate() : LocalDate.now();
        LocalDate nextRunDate  = startDate;
        String frequency       = req.frequency() != null ? req.frequency().toUpperCase() : StandingOrder.FREQ_MONTHLY;

        String soNo = String.format("STO-%s-%s",
                LocalDate.now().getYear(),
                UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        StandingOrder order = new StandingOrder(
                UUID.randomUUID(), soNo,
                sourceAccount.getAccountId(), sourceAccount.getAccountNo(),
                targetAccount.getAccountId(), targetAccount.getAccountNo(),
                req.memberId(), req.amount(), frequency,
                req.dayOfMonth(), req.dayOfWeek(),
                startDate, nextRunDate, req.endDate(),
                0, 0, req.description(),
                StandingOrder.STATUS_ACTIVE,
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        StandingOrder saved = standingOrderRepository.save(order);
        log.info("Created Standing Order {}: {} ETB {} from {} to {}", soNo, req.amount(), frequency,
                req.sourceAccountNo(), req.targetAccountNo());
        return saved;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override @Transactional
    public StandingOrder pause(UUID standingOrderId) {
        StandingOrder order = getOrThrow(standingOrderId);
        if (!order.isActive()) throw new IllegalStateException("Only ACTIVE orders can be paused.");
        order.pause();
        return standingOrderRepository.save(order);
    }

    @Override @Transactional
    public StandingOrder resume(UUID standingOrderId) {
        StandingOrder order = getOrThrow(standingOrderId);
        if (!order.isPaused()) throw new IllegalStateException("Only PAUSED orders can be resumed.");
        order.resume();
        return standingOrderRepository.save(order);
    }

    @Override @Transactional
    public StandingOrder cancel(UUID standingOrderId) {
        StandingOrder order = getOrThrow(standingOrderId);
        if (order.isCancelled()) throw new IllegalStateException("Order is already cancelled.");
        order.cancel();
        return standingOrderRepository.save(order);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override @Transactional(readOnly = true)
    public StandingOrder getById(UUID standingOrderId) { return getOrThrow(standingOrderId); }

    @Override @Transactional(readOnly = true)
    public List<StandingOrder> getByMemberId(UUID memberId) {
        return standingOrderRepository.findByMemberId(memberId);
    }

    @Override @Transactional(readOnly = true)
    public List<StandingOrder> getByPhoneNumber(String phoneNumber) {
        List<Account> accounts = accountRepository.findByPhoneNumber(phoneNumber);
        if (accounts.isEmpty()) {
            log.warn("[STANDING-ORDER] No accounts found for phone: {}", phoneNumber);
            return List.of();
        }
        // Use the userId from the first matched account (all accounts share same memberId)
        UUID memberId = accounts.get(0).getUserId();
        return standingOrderRepository.findByMemberId(memberId);
    }

    @Override @Transactional(readOnly = true)
    public List<StandingOrder> getAll() { return standingOrderRepository.findAll(); }

    // ── Daily Sweep Runner ────────────────────────────────────────────────────

    /**
     * Scheduled daily sweep — fires at 00:10 every morning (10 minutes after EOD midnight batch).
     * Can also be triggered manually via REST for same-day backdating.
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void runScheduledDailySweeps() {
        log.info("[SWEEP] Scheduled daily standing order sweep triggered for {}", LocalDate.now());
        runDailySweeps(LocalDate.now());
    }

    @Override
    @Transactional
    public SweepRunResult runDailySweeps(LocalDate runDate) {
        List<StandingOrder> dueOrders = standingOrderRepository.findDueOrders(runDate);
        log.info("[SWEEP] Running standing order sweeps for {}. Due orders: {}", runDate, dueOrders.size());

        int success = 0;
        int failed  = 0;
        BigDecimal totalSwept = BigDecimal.ZERO;

        for (StandingOrder order : dueOrders) {
            try {
                Account source = accountRepository.findByAccountNo(order.getSourceAccountNo())
                        .orElse(null);
                Account target = accountRepository.findByAccountNo(order.getTargetAccountNo())
                        .orElse(null);

                if (source == null || target == null) {
                    log.warn("[SWEEP] Skipping {} — source or target account not found.", order.getStandingOrderNo());
                    order.recordFailure();
                    standingOrderRepository.save(order);
                    failed++;
                    continue;
                }

                BigDecimal available = source.getBookBalance().subtract(source.getLienHoldAmount());
                if (available.compareTo(order.getAmount()) < 0) {
                    log.warn("[SWEEP] Insufficient funds for {}. Available: {} ETB, Required: {} ETB",
                            order.getStandingOrderNo(), available, order.getAmount());
                    order.recordFailure();
                    standingOrderRepository.save(order);
                    failed++;
                    continue;
                }

                // Execute sweep — debit source, credit target
                source.setBookBalance(source.getBookBalance().subtract(order.getAmount()));
                target.setBookBalance(target.getBookBalance().add(order.getAmount()));
                accountRepository.save(source);
                accountRepository.save(target);

                order.advanceNextRunDate();
                standingOrderRepository.save(order);

                totalSwept = totalSwept.add(order.getAmount());
                success++;

                log.info("[SWEEP] Executed {}: {} ETB from {} → {}",
                        order.getStandingOrderNo(), order.getAmount(),
                        order.getSourceAccountNo(), order.getTargetAccountNo());

            } catch (Exception ex) {
                log.error("[SWEEP] Exception executing {}: {}", order.getStandingOrderNo(), ex.getMessage());
                order.recordFailure();
                standingOrderRepository.save(order);
                failed++;
            }
        }

        log.info("[SWEEP] Complete: Date={}, Total={}, Success={}, Failed={}, SweptAmount={} ETB",
                runDate, dueOrders.size(), success, failed, totalSwept);

        return new SweepRunResult(runDate, dueOrders.size(), success, failed, totalSwept);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private StandingOrder getOrThrow(UUID id) {
        return standingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Standing order not found: " + id));
    }
}
