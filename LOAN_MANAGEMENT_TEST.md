# 💰 Loan Management Service (`loan-management-service`) API Test Suite

This document contains step-by-step cURL commands and JSON payloads to test Loan Disbursement, Dual-Control Maker-Checker Disbursement Approval, Amortization Schedule Generation, Waterfall Repayments, and Loan Audit Logs.

---

## 📱 Test Actors & Roles Used

| Role | Actor | Target SACCO | Phone Number |
|---|---|---|---|
| **TELLER / EMPLOYEE** | Awach Loan Officer (Maker) | Awach SACCO (`sacco_awach_sacco`) | `+251961741038` |
| **SACCO_ADMIN** | Awach SACCO Admin (Checker) | Awach SACCO (`sacco_awach_sacco`) | `+251911109512` |
| **PRIMARY MEMBER** | Abebe Bikila (Borrower) | Awach SACCO (`sacco_awach_sacco`) | `+251955336000` |

---

## 🧪 Sequential API Test Execution Flow

### 1️⃣ Initiate Loan Disbursement (Maker Teller)
Teller initiates disbursement for an approved loan application. Creates account with status `PENDING_DISBURSEMENT`.

- **URL**: `POST http://localhost:8086/api/v1/loan-mgmt/disburse`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `X-Idempotency-Key: DISB-KEY-001`
  - `Authorization: Bearer <TELLER_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8086/api/v1/loan-mgmt/disburse \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "X-Idempotency-Key: DISB-KEY-001" \
  -H "Authorization: Bearer <TELLER_TOKEN>" \
  -d '{
    "applicationId": "<APPLICATION_ID>",
    "userId": "<MEMBER_USER_ID>",
    "productId": "<PRODUCT_ID>",
    "amount": 50000.00,
    "interestRatePct": 14.00,
    "termMonths": 12,
    "repaymentFrequency": "MONTHLY",
    "interestType": "REDUCING_BALANCE",
    "memberPhone": "+251955336000"
  }'
```

**JSON Payload:**
```json
{
  "applicationId": "<APPLICATION_ID>",
  "userId": "<MEMBER_USER_ID>",
  "productId": "<PRODUCT_ID>",
  "amount": 50000.00,
  "interestRatePct": 14.00,
  "termMonths": 12,
  "repaymentFrequency": "MONTHLY",
  "interestType": "REDUCING_BALANCE",
  "memberPhone": "+251955336000"
}
```

**Expected Response (200 OK):**
Save `accountId` (e.g. `c7f...-uuid`) for approval step.

---

### 2️⃣ Dual-Control Disbursement Approval (Checker Admin)
Supervising admin approves disbursement. Transitions status to `DISBURSED`, generates 12 monthly amortization installments, and sends SMS to `+251955336000`. Enforces Maker-Checker self-approval guard.

- **URL**: `PATCH http://localhost:8086/api/v1/loan-mgmt/disburse/<LOAN_ACCOUNT_ID>/approve`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X PATCH http://localhost:8086/api/v1/loan-mgmt/disburse/<LOAN_ACCOUNT_ID>/approve \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```

---

### 3️⃣ Query Amortization Repayment Schedule
Retrieves 12 monthly repayment installments generated for loan account `<LOAN_ACCOUNT_ID>`.

- **URL**: `GET http://localhost:8086/api/v1/loan-mgmt/schedule/<LOAN_ACCOUNT_ID>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8086/api/v1/loan-mgmt/schedule/<LOAN_ACCOUNT_ID> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```

---

### 4️⃣ Process Waterfall Loan Repayment
Processes ETB 4,500.00 repayment installment. Allocates funds using Core Banking waterfall priority: Penalty -> Fee -> Interest -> Principal.

- **URL**: `POST http://localhost:8086/api/v1/loan-mgmt/repay`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <TELLER_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8086/api/v1/loan-mgmt/repay \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <TELLER_TOKEN>" \
  -d '{
    "accountId": "<LOAN_ACCOUNT_ID>",
    "amountPaid": 4500.00,
    "paymentChannel": "TELLER_COUNTER",
    "remarks": "Monthly installment payment",
    "memberPhone": "+251955336000"
  }'
```

**JSON Payload:**
```json
{
  "accountId": "<LOAN_ACCOUNT_ID>",
  "amountPaid": 4500.00,
  "paymentChannel": "TELLER_COUNTER",
  "remarks": "Monthly installment payment",
  "memberPhone": "+251955336000"
}
```

---

### 5️⃣ Query Loan Audit Logs
Inspects loan disbursement and approval audit entries for account `<ACCOUNT_NO>`.

- **URL**: `GET http://localhost:8086/api/v1/loan-mgmt/audit/account/<ACCOUNT_NO>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8086/api/v1/loan-mgmt/audit/account/<ACCOUNT_NO> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```
