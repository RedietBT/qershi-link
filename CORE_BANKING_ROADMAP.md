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
  - [x] Evening **Till Closure & Physical Banknote Counting reconciliation** (denominations: 200, 100, 50, 10, 5 ETB) with variance detection (`till_cash_reconciliations`).
  - [x] Atomic integration with `CashTransactionService` (cash movement validation & debit/credit on OTC deposits and withdrawals).
- [x] **Frontend Deliverables**
  - [x] `BranchManagementPage.jsx`: Branch roster, add/edit branch modal, active vault count, and discretionary limits.
  - [x] `TellerDrawerPage.jsx`: Live drawer cash balance, morning drawer opening, and interactive physical banknote denomination counter modal.
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
- [x] **Account Dormancy Rule**
  - [x] Auto-transition accounts with no activity $>180$ days to `DORMANT`.
- [x] **Frontend Deliverables**
  - [x] `EodControlPage.jsx`: Run EOD batch, inspect step logs, view business date status.
  - [x] `LoanDelinquencyDashboard.jsx`: PAR aging breakdown pie/bar charts, overdue member list.

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
- [ ] **Member Peer Guarantor Management**
  - [ ] Update `loan-origination-service` to accept `guarantors` array (`guarantor_user_id`, `savings_account_no`, `guaranteed_amount`).
  - [ ] Verify guarantor has sufficient unencumbered savings balance.
  - [ ] On loan disbursement: Automatically invoke `account-service` to place **Lien Hold** on guarantor accounts.
  - [ ] On final loan repayment/settlement: Automatically release guarantor liens.
- [ ] **Fee & Tariff Engine**
  - [ ] Create `tariffs` table (`transaction_type`, `fee_type`: `FLAT`, `PERCENTAGE`, `value`, `min_fee`, `max_fee`, `fee_gl_code`).
  - [ ] Deduct configured fees automatically during cash withdrawal and internal transfers.
- [ ] **Withholding Tax (WHT) on Savings Interest**
  - [ ] Calculate statutory 5% withholding tax during monthly interest capitalization.
  - [ ] Post: `DEBIT Interest Payable` | `CREDIT Member Savings (95%)` | `CREDIT WHT Payable to Government (5%)`.
- [ ] **Frontend Deliverables**
  - [x] `MakerCheckerPolicyPage.jsx`: Dedicated SACCO Maker-Checker & Four-Eyes policy management screen.
  - [ ] `GuarantorPledgingSection.jsx`: Integrated into loan application form with real-time balance check.
  - [ ] `TariffManagementPage.jsx`: Configure transaction fees, commissions, and tax rules.

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
