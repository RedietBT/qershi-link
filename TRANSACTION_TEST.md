# 💸 Transaction Management Service (`transaction-management-service`) API Test Suite

This document contains step-by-step cURL commands and JSON payloads to test over-the-counter Cash Deposits, Cash Withdrawals, Member-to-Member Transfers, GL Double-Entry Journal Postings, and Transaction Audit Logs.

---

## 📱 Test Actors & Roles Used

| Role | Actor | Target SACCO | Phone Number |
|---|---|---|---|
| **TELLER / EMPLOYEE** | Awach SACCO Teller (Operator) | Awach SACCO (`sacco_awach_sacco`) | `+251961741038` |
| **PRIMARY MEMBER** | Abebe Bikila (Sender / Depositor) | Awach SACCO (`sacco_awach_sacco`) | `+251955336000` |
| **SACCO_ADMIN** | Awach SACCO Admin | Awach SACCO (`sacco_awach_sacco`) | `+251911109512` |

---

## 🧪 Sequential API Test Execution Flow

### 1️⃣ Process Cash Deposit
Teller deposits ETB 10,000.00 into member account `<ACCOUNT_NO>`. Updates available balance, posts General Ledger journal lines (`DEBIT Teller Vault Cash` / `CREDIT Member Savings`), and dispatches SMS to `+251955336000`.

- **URL**: `POST http://localhost:8083/api/v1/transactions/deposit`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `X-Idempotency-Key: DEP-KEY-001`
  - `Authorization: Bearer <TELLER_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8083/api/v1/transactions/deposit \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "X-Idempotency-Key: DEP-KEY-001" \
  -H "Authorization: Bearer <TELLER_TOKEN>" \
  -d '{
    "accountNo": "<ACCOUNT_NO>",
    "amount": 10000.00,
    "narration": "Teller OTC Cash Deposit"
  }'
```

**JSON Payload:**
```json
{
  "accountNo": "<ACCOUNT_NO>",
  "amount": 10000.00,
  "narration": "Teller OTC Cash Deposit"
}
```

---

### 2️⃣ Process Cash Withdrawal
Teller withdraws ETB 2,500.00 from member account `<ACCOUNT_NO>`. Verifies balance, posts GL journal lines, and dispatches SMS to `+251955336000`.

- **URL**: `POST http://localhost:8083/api/v1/transactions/withdraw`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `X-Idempotency-Key: WTH-KEY-001`
  - `Authorization: Bearer <TELLER_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8083/api/v1/transactions/withdraw \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "X-Idempotency-Key: WTH-KEY-001" \
  -H "Authorization: Bearer <TELLER_TOKEN>" \
  -d '{
    "accountNo": "<ACCOUNT_NO>",
    "amount": 2500.00,
    "narration": "Teller OTC Cash Withdrawal"
  }'
```

---

### 3️⃣ Process Member-to-Member Internal Transfer
Transfers ETB 1,000.00 from `<SENDER_ACCOUNT_NO>` to `<RECEIVER_ACCOUNT_NO>`.

- **URL**: `POST http://localhost:8083/api/v1/transactions/transfer`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `X-Idempotency-Key: TRF-KEY-001`
  - `Authorization: Bearer <TELLER_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8083/api/v1/transactions/transfer \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "X-Idempotency-Key: TRF-KEY-001" \
  -H "Authorization: Bearer <TELLER_TOKEN>" \
  -d '{
    "senderAccountNo": "<SENDER_ACCOUNT_NO>",
    "receiverAccountNo": "<RECEIVER_ACCOUNT_NO>",
    "amount": 1000.00,
    "narration": "Internal member savings transfer"
  }'
```

---

### 4️⃣ Query Transaction History by Account
Retrieves transaction ledger history for account `<ACCOUNT_NO>`.

- **URL**: `GET http://localhost:8083/api/v1/transactions/account/<ACCOUNT_NO>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8083/api/v1/transactions/account/<ACCOUNT_NO> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```

---

### 5️⃣ Query Transaction Audit Logs
Inspects financial audit logs recorded for cash and transfer operations.

- **URL**: `GET http://localhost:8083/api/v1/transactions/audit/account/<ACCOUNT_NO>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8083/api/v1/transactions/audit/account/<ACCOUNT_NO> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```
