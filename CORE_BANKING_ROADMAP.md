# 🏦 Qershi Link Enterprise Core Banking System (CBS)
## 7-Day Comprehensive Implementation Master Plan & Checklist

> **Target Completion Window**: 7 Operational Days  
> **Standard**: Core Banking Compliance (Temenos Transact / Finacle / Mifos standard)  
> **Target Deployments**: Multi-Tenant SACCOs, Microfinance Institutions, and Cooperatives

---

## 📅 Day-by-Day Implementation Schedule Overview

```
┌──────────────┬───────────────────────────────────────────┬────────────────────────────────────────┐
│ Day          │ Core Banking Feature Domain               │ Key Deliverables                       │
├──────────────┼───────────────────────────────────────────┼────────────────────────────────────────┤
│ Day 1 (Mon)  │ Branch & Teller Till (Cash Drawer) Ops    │ Branch CRUD, Vault-to-Till, Day-End    │
│ Day 2 (Tue)  │ EOD Batch, Interest Accrual & PAR Aging   │ Midnight Batch, Accruals, PAR 1-90+    │
│ Day 3 (Wed)  │ Dynamic Chart of Accounts & Fin. Reports  │ COA Tree, Trial Balance, P&L, BalSheet │
│ Day 4 (Thu)  │ Peer Guarantor Liens & Fee/Tax Engine     │ Social Guarantees, Tariffs, 5% WHT     │
│ Day 5 (Fri)  │ Share Capital, Dividends & Standing Orders│ Mandatory Shares, AGM Dividends, Sweeps│
│ Day 6 (Sat)  │ Kafka Event Bus & Dynamic UI SMS Gateway  │ Kafka EDA, Multi-Provider SMS Engine   │
│ Day 7 (Sun)  │ Open Banking Webhooks & Full Stress Test  │ Telebirr/CBE Webhooks, E2E Regression  │
└──────────────┴───────────────────────────────────────────┴────────────────────────────────────────┘
```

---

## 📋 Comprehensive Feature Checklist

### 🏢 Day 1: Branch Management & Teller Till Hierarchy
- [x] **Branch Directory & Entity Model**
  - [x] Create `branches` table per tenant schema (`branch_id`, `branch_code`, `name`, `region`, `address`, `phone`, `manager_id`, `vault_gl_code`, `discretionary_limit`, `status`).
  - [x] Add `BranchController` REST endpoints (List, Get, Create, Update, Activate/Deactivate).
  - [x] Seed Head Office branch (`001`) and setup branch resolution in tenant provisioning.
- [x] **Cash Vault & Till Hierarchy**
  - [x] Create `teller_tills` table (`till_id`, `branch_id`, `teller_user_id`, `till_gl_code`, `opening_cash`, `current_cash`, `status`).
  - [x] Morning **Vault-to-Till Opening** with live cash position tracking.
  - [x] Evening **Blind Till Closure & Banknote Denominations (Temenos/Finacle Tier-1 standard)**:
    - [x] Blind reconciliation (system ledger hidden to prevent teller guessing).
    - [x] Itemized denomination persistence (`till_denominations` table for 200, 100, 50, 10, 5 ETB notes and coins).
    - [x] Automated double-entry GL variance adjustments (`5090-CASH-SHORTAGE-EXPENSE` or `4090-CASH-OVERAGE-INCOME`).
    - [x] Four-Eyes Supervisor sign-off workflow when variance exceeds threshold (> 100.00 ETB).
    - [x] Complete closing audit trail (`till_closing_logs` table).
  - [x] Atomic integration with `CashTransactionService` (cash movement validation & debit/credit on OTC deposits and withdrawals).
- [x] **Frontend Deliverables**
  - [x] `BranchManagementPage.jsx`: Branch roster, add/edit branch modal, active vault count, and discretionary limits.
  - [x] `TellerDrawerPage.jsx`: Live drawer cash balance, morning drawer opening, blind physical banknote denomination counter modal with supervisor peek & variance audit alerts.
  - [x] Connected routes (`/branches`, `/transactions/till`) and navigation items in `Sidebar.jsx` and `CashDeskPage.jsx`.

---

### 🌙 Day 2: End-of-Day (EOD) Batch Runner, Accrual & PAR Delinquency
- [x] **Automated EOD / BOD Batch Runner**
  - [x] Create `system_business_date` state & batch coordinator in `account-management-service`.
  - [x] Automated scheduled job (`@Scheduled(cron = "0 0 0 * * ?")`) with manual trigger override.
  - [x] Lock transaction posting during EOD cutoff.
- [x] **Daily Interest Accrual & Monthly Capitalization**
  - [x] Compute daily savings interest: $\text{Daily Accrual} = \frac{\text{Cleared Balance} \times \text{Interest Rate}}{365}$.
  - [x] Post accounting accrual: `DEBIT Interest Expense` / `CREDIT Interest Payable Accrued`.
  - [x] End-of-Month Capitalization routine crediting accrued interest to member accounts.
- [x] **Portfolio at Risk (PAR) & Delinquency Aging**
  - [x] Calculate Days Past Due (DPD) on all active loan amortization schedules.
  - [x] Classify loans into regulatory buckets:
    - [x] `Current` (0 DPD)
    - [x] `Watchlist / PAR 1-30` (1–30 DPD)
    - [x] `Substandard / PAR 31-60` (31–60 DPD, 25% provisioning)
    - [x] `Doubtful / PAR 61-90` (61–90 DPD, 50% provisioning)
    - [x] `Loss / NPL / PAR 90+` (>90 DPD, 100% provisioning)
- [x] **Account Dormancy Rule & KYC Reactivation Lifecycle (Temenos/Finacle/WOCCU standard)**
  - [x] Auto-transition accounts with no activity $>180$ days to `DORMANT` in EOD batch orchestrator.
  - [x] Automated SMS security warning dispatched to member upon dormancy transition.
  - [x] Strict debit/withdrawal blocking to prevent insider fraud.
  - [x] Maker-Checker in-person biometric / KYC re-verification workflow with Anti-Self-Approval enforcement.
  - [x] `last_activity_date`, `dormancy_date`, `reactivation_status`, maker/checker notes and timestamps persisted in `accounts`.
- [x] **Frontend Deliverables**
  - [x] `EodControlPage.jsx`: Run EOD batch, inspect step logs, view business date status.
  - [x] `LoanDelinquencyDashboard.jsx`: PAR aging breakdown pie/bar charts, overdue member list.
  - [x] `MemberAccountsTab.jsx`: Live dormancy status alert banner, withdrawals blocked warning, and KYC Reactivation trigger modal.
  - [x] `ReactivateAccountModal.jsx`: Maker in-person KYC biometric verification submission & Checker supervisor review modal.
    - [x] `PendingAuthorizationsPage.jsx`: Tabbed Four-Eye authorization queues for both new account openings and dormancy KYC reactivations.
- [x] **IFRS 9 / NBE Regulatory Loan Loss Provisioning (Gap 4 — NBE Directive & IFRS 9 ECL)**
  - [x] Monthly portfolio impairment calculation with mandatory NBE five-stage risk classification:
    - [x] `Pass` (0–29 DPD): 1.0% general reserve
    - [x] `Special Mention` (30–89 DPD): 5.0% specific reserve
    - [x] `Substandard` (90–179 DPD): 20.0% specific reserve
    - [x] `Doubtful` (180–359 DPD): 50.0% specific reserve
    - [x] `Loss` (360+ DPD): 100.0% full write-off reserve
  - [x] Balanced GL double-entry posted automatically: `DEBIT GL 5030 — Loan Impairment Loss Expense` / `CREDIT GL 1039 — Allowance for Credit Losses (Contra-Asset)`.
  - [x] Idempotent month-end run with unique GL journal posting reference (`JE-IFRS9-YYYYMMDD-XXXXXXXX`).
  - [x] Per-loan ECL provision lines stored with full audit trail in `loan_impairment_provision_lines`.
  - [x] Month-end step wired into `EodBatchOrchestrator` Step 5 (fires only when `isMonthEnd=true`) via REST call to loan service.
  - [x] REST API: `/api/v1/loans/ifrs9-provisioning/run`, `/latest`, `/history`, `/{runId}/lines`.
  - [x] `Ifrs9ComplianceCard.jsx`: Premium compliance card on the delinquency dashboard with animated stage breakdown bars, expandable GL journal entry panel, and manual trigger button.
- [x] **Fixed Term Deposit (FD) Contracts & Early Penalty Break (Gap 5 — Temenos Transact / Finacle FD Standard)**
  - [x] Multi-tenor Fixed Term Deposit lifecycle (`term_deposit_contracts` table, `V9__create_term_deposit_contracts.sql`):
    - [x] Configurable tenors (3, 6, 12, 24, 36 months) with preferential interest rates (e.g. 10.5%–14.5% p.a.).
    - [x] Double-entry GL integration on opening: `DEBIT GL 1010 — Member Savings Account` / `CREDIT GL 2060 — Term Deposit Liability`.
    - [x] Automatic maturity processing with auto-rollover toggle or principal + accrued interest payout to savings.
    - [x] Early break / premature termination engine with configurable penalty rate deducted from accrued interest.
    - [x] Supervisor Four-Eyes authorization workflow for early termination (`maker != checker`).
  - [x] EOD Batch integration: `TermDepositService.processMaturedContracts()` wired into `EodBatchOrchestrator` step.
  - [x] REST API: `POST /api/v1/term-deposits/open`, `GET /account/{accountNo}`, `GET /active`, `GET /{contractId}`, `POST /{contractId}/break-early`, `POST /process-matured`.
  - [x] Frontend deliverables:
    - [x] `TermDepositPage.jsx`: Active FD portfolio dashboard, status filters, premature break trigger modal, and maturity projections.
    - [x] `TermDepositCard.jsx`: Interactive card with maturity progress bar, accrued interest counter, and auto-rollover indicator.
    - [x] `OpenTermDepositModal.jsx`: FD opening modal with tenor selection, interest rate calculations, and live GL entry preview.
    - [x] Navigation: Integrated into `Sidebar.jsx` and `AccountManagementDeck.jsx`.

---

### 📊 Day 3: Dynamic Chart of Accounts (COA) & Financial Statements
- [x] **Dynamic Chart of Accounts Tree**
  - [x] Create `chart_of_accounts` table (`gl_code`, `account_name`, `account_type`: `ASSET`, `LIABILITY`, `EQUITY`, `REVENUE`, `EXPENSE`, `parent_gl_code`, `is_reconciled`).
  - [x] Seed default Standard Cooperative Banking Chart of Accounts.
  - [x] REST API to query COA hierarchy as an expandable JSON tree.
- [x] **Financial Reporting Generation Engine**
  - [x] **Trial Balance**: Aggregates all debit/credit postings per GL code; verifies $\sum \text{Debits} = \sum \text{Credits}$.
  - [x] **Balance Sheet**: Assets = Liabilities + Member Equity (Share Capital, Retained Earnings).
  - [x] **Profit & Loss (P&L)**: Interest Income + Fee Income - Interest Expense - Operating Expenses = Net Surplus.
- [x] **Frontend Deliverables**
  - [x] `ChartOfAccountsPage.jsx`: Expandable tree-view of GL accounts with live balances.
  - [x] `FinancialReportsPage.jsx`: Tabbed viewer for Trial Balance, Balance Sheet, and P&L with CSV / PDF print export.

---

### 🤝 Day 4: Peer Guarantors, Maker-Checker Policy Engine & Fee/Tax
- [x] **SACCO Maker-Checker & Policy Rules Engine**
  - [x] Create `sacco_maker_checker_rules` table (`V6__create_maker_checker_rules.sql`).
  - [x] Configurable Four-Eyes toggles for Member Onboarding, Account Opening, Account Freeze, Loan Approval, and Disbursement.
  - [x] Configurable single-transaction supervisor override threshold & daily account limit threshold.
  - [x] Enforce anti-self-approval rule (`maker != checker`).
  - [x] REST endpoints: `GET /api/v1/sacco-config/maker-checker-rules` and `PUT /api/v1/sacco-config/maker-checker-rules`.
  - [x] `MakerCheckerPolicyPage.jsx`: Full SACCO governance UI with interactive domain toggles, threshold inputs, and anti-self-approval enforcement banner.
- [x] **Account Product Risk Limits & Real-Time Enforcement Engine**
  - [x] Create `product_maker_checker_rules` and `V7__add_daily_withdrawn_tracking_to_accounts.sql`.
  - [x] Storage ceiling enforcement: Rejects deposits/inbound transfers if resulting balance exceeds product's `max_balance_limit`.
  - [x] Single-withdrawal supervisor threshold: Enforces Four-Eyes Maker-Checker authorization if withdrawal exceeds `single_withdrawal_limit`.
  - [x] Daily cumulative withdrawal limit: Tracks daily debits and blocks transactions exceeding `daily_withdrawal_limit`.
  - [x] Minimum operating balance floor: Enforces unwithdrawable minimum contractual balance.
  - [x] Dynamic UI: Modal for creating and editing per-account-type rules with quick presets (Student, Women, General, etc.) in `AccountsWorkflowTab.jsx`.
- [x] **Member Peer Guarantor Management**
  - [x] Update `loan-origination-service` to accept `guarantors` array (`guarantor_user_id`, `savings_account_no`, `guaranteed_amount`).
  - [x] Verify guarantor has sufficient unencumbered savings balance via real-time gRPC check.
  - [x] On loan disbursement: Automatically invoke `account-service` (`PlaceLien`) to place monetary **Lien Hold** on guarantor accounts.
  - [x] On final loan repayment/settlement: Automatically release guarantor liens (`ReleaseLien`) upon loan account closure.
- [x] **Dedicated Pricing & Fee Engine Microservice (`pricing-fee-service`: HTTP 8087, gRPC 9087)**
  - [x] Decoupled into Hexagonal Architecture microservice with multi-tenancy & JWT security.
  - [x] Create `tariffs` and `interest_tax_deduction_logs` tables and seed default cooperative fee policies.
  - [x] High-performance gRPC interfaces for `CalculateFee`, `CalculateWithholdingTax`, and `GetActiveTariffs`.
  - [x] Dynamic fee calculation for Cash Withdrawals, Member Transfers, and Loan Processing.
  - [x] **Tiered / Amount-Bracket Slab Pricing Engine (Tier-1 CBS Temenos/Finacle Standard)**:
    - [x] Multi-tenant `tariff_slabs` table schema (`from_amount`, `to_amount`, `fee_type`, `fee_value`, `min_fee`, `max_fee`, `slab_order`).
    - [x] Sequential volume brackets evaluation with support for uncapped upper limits, flat ETB fees, percentage rates with min/max caps, and graceful fallbacks.
    - [x] Full backward compatibility with legacy FLAT and PERCENTAGE tariffs.
    - [x] Seeded `TAR-WTH-TIER` enterprise ladder: 0-1k (5 ETB), 1k-10k (15 ETB), 10k-50k (25 ETB), 50k+ (0.25% max 100 ETB).
    - [x] Interactive UI: Visual Slab Ladder Editor with presets in `TariffFormModal.jsx`, live matched-bracket breakdown in `TariffSimulatorCard.jsx`, and tiered badges in `TariffManagementPage.jsx`.
  - [x] Loan Origination Fee Option A (Net Disbursement): Upfront fee deducted at source, credited to Fee Income GL (4021), with principal schedule intact.
- [x] **Withholding Tax (WHT) on Savings Interest**
  - [x] Calculate statutory 5% withholding tax during monthly interest capitalization via `pricing-fee-service` gRPC.
  - [x] Post: `DEBIT Interest Payable (2051)` | `CREDIT Member Savings (95%)` | `CREDIT WHT Payable to Government (2091, 5%)`.
- [x] **Platform Governance & Central Tooling Integration**
  - [x] Seeded `TARIFF_VIEW` and `TARIFF_MANAGE` permissions in `identity-auth-service` RBAC (`V15__seed_pricing_and_tariff_permissions.sql`).
  - [x] Added `09_pricing.sql` and updated `08_seed_data.sql` in `tenant-template/` for automated schema provisioning on new SACCO onboarding.
  - [x] Registered in Central `swagger-api-hub` (port 8090) with backend proxy to `http://pricing-fee-service:8087/v3/api-docs`.
- [x] **Frontend Deliverables**
  - [x] `MakerCheckerPolicyPage.jsx`: Dedicated SACCO Maker-Checker & Four-Eyes policy management screen.
  - [x] `GuarantorPledgingSection.jsx`: Integrated into loan application form with real-time balance check.
  - [x] `src/features/pricing/`: Dedicated UI feature module with `pricingApi.js`, `pricingRoutes.jsx`, and all action buttons wrapped in `PermissionGuard`.

---

### 📈 Day 5: Share Capital, Dividends & Standing Orders
- [ ] **Share Capital Management**
  - [ ] Enforce minimum mandatory shares policy during member onboarding (e.g. 5 shares @ 1,000 ETB).
  - [ ] Track share certificate serial numbers and transferability between members.
- [ ] **Annual AGM Dividend Distribution Engine**
  - [ ] Dividend calculation routine: takes Net Profit for the year and approved Dividend % (e.g. 12%).
  - [ ] Prorates dividend based on each member's weighted average share balance.
  - [ ] Batch execution posting dividends directly into member savings accounts.
- [ ] **Standing Orders (Automated Recurring Sweeps)**
  - [ ] Create `standing_orders` table (`source_account`, `target_account`, `amount`, `frequency`, `next_run_date`, `status`).
  - [ ] Scheduled runner executing daily sweeps for monthly member contributions and loan repayments.
- [ ] **Frontend Deliverables**
  - [ ] `ShareCapitalPage.jsx`: Share holdings, purchase shares modal, transfer shares.
  - [ ] `DividendDistributionModal.jsx`: Simulation calculator & batch distribution trigger.
  - [ ] `StandingOrdersPage.jsx`: Create and monitor automated recurring sweeps.

---

### ⚡ Day 6: Kafka Event Bus & Dynamic UI SMS Gateway Provider
- [ ] **Apache Kafka Event-Driven Architecture**
  - [ ] Deploy Kafka broker configuration in docker-compose / Kubernetes.
  - [ ] Publish domain events from:
    - [ ] `transaction-management-service`: `TransactionCompletedEvent`
    - [ ] `loan-management-service`: `LoanDisbursedEvent`, `RepaymentReceivedEvent`
    - [ ] `account-management-service`: `AccountOpenedEvent`, `InterestCapitalizedEvent`
  - [ ] `notification-service` consumes events asynchronously with retry backoff and Dead Letter Queue (DLQ).
- [ ] **Dynamic Multi-Tenant SMS Gateway Engine**
  - [ ] Create `sms_gateway_configs` table (`sacco_id`, `provider`: `AFROMESSAGE`, `ETHIO_TELECOM`, `INFOBIP`, `CUSTOM_WEBHOOK`, `api_key_encrypted`, `sender_id`, `is_active`).
  - [ ] Customizable SMS Templates with dynamic tokens (`{{memberName}}`, `{{amount}}`, `{{accountNo}}`, `{{balance}}`).
- [ ] **Frontend Deliverables**
  - [ ] `SmsGatewayConfigPage.jsx`: Tenant configuration screen to select provider, enter live API credentials, and test connection.
  - [ ] `SmsTemplateEditor.jsx`: Customize SMS notification text for deposits, withdrawals, and loans.

---

### 🌐 Day 7: Open Banking Webhooks, Integrations & Final Stress Test
- [ ] **Open Banking & Mobile Wallet Webhook Ingress**
  - [ ] `POST /api/v1/integrations/payments/webhook`: Inbound C2B deposit webhook for Telebirr, CBE Birr, and Chapa.
  - [ ] `POST /api/v1/integrations/payouts`: Outbound B2C fund disbursement to member mobile wallets.
  - [ ] HMAC Signature verification and API Key security layer.
- [ ] **Anti-Money Laundering (AML) Cash Threshold Alerts**
  - [ ] Flag and alert transactions exceeding threshold (e.g. > 200,000 ETB) for Cash Transaction Reporting (CTR).
- [ ] **End-to-End Stress Test & Verification**
  - [ ] Full compilation across all 10 microservices (`BUILD SUCCESS`).
  - [ ] Execute all automated tests across all modules.
  - [ ] Verify Vite production bundle size and browser subagent end-to-end traversal.

---

## 🎯 Verification Criteria for "Core Banking System" Readiness

| Checkpoint | Target Standard |
|---|---|
| **Zero Anonymous Cash** | Every cent in the platform is assigned to a verified Branch, Vault, or Teller Till. |
| **Zero Out-of-Balance Books** | Every transaction produces balanced debits and credits ($\sum D = \sum C$). |
| **Zero Manual Interest Math** | EOD batch automatically computes accruals and capitalizations. |
| **Zero Hidden Delinquency** | PAR aging buckets identify overdue loans within 24 hours of missed due dates. |
| **Zero Unchecked Pledges** | Guarantor savings accounts are automatically locked with lien holds upon loan issuance. |
| **Zero Lag on Transactions** | SMS notifications are dispatched asynchronously via Kafka without blocking API requests. |
