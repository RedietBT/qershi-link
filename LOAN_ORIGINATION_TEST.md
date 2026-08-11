# 📝 Loan Origination Service (`loan-origination-service`) API Test Suite

This document contains step-by-step cURL commands and JSON payloads to test Deposit Product setup, Loan Application submission, Maker-Checker Loan Assessment, and Credit Scoring in `loan-origination-service`.

---

## 📱 Test Actors & Roles Used

| Role | Actor | Target SACCO | Phone Number |
|---|---|---|---|
| **SACCO_ADMIN** | Awach SACCO Admin (Checker) | Awach SACCO (`sacco_awach_sacco`) | `+251911109512` |
| **TELLER / EMPLOYEE** | Awach Loan Officer (Maker) | Awach SACCO (`sacco_awach_sacco`) | `+251961741038` |
| **PRIMARY MEMBER** | Abebe Bikila (Applicant) | Awach SACCO (`sacco_awach_sacco`) | `+251955336000` |

---

## 🧪 Sequential API Test Execution Flow

### 1️⃣ Create Account Product Factory Entry
Sets up a standard Savings/Deposit Product (`SAV01`) in `account-management-service`.

- **URL**: `POST http://localhost:8082/api/v1/accounts/products`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8082/api/v1/accounts/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>" \
  -d '{
    "productName": "Voluntary Member Savings",
    "category": "SAVINGS",
    "currency": "ETB",
    "interestRatePa": 7.00,
    "postingFrequency": "MONTHLY",
    "minOperatingBalance": 100.00,
    "minMonthlyContribution": 500.00,
    "termPeriodMonths": 12,
    "earlyWithdrawalPenaltyPct": 1.50
  }'
```

**JSON Payload:**
```json
{
  "productName": "Voluntary Member Savings",
  "category": "SAVINGS",
  "currency": "ETB",
  "interestRatePa": 7.00,
  "postingFrequency": "MONTHLY",
  "minOperatingBalance": 100.00,
  "minMonthlyContribution": 500.00,
  "termPeriodMonths": 12,
  "earlyWithdrawalPenaltyPct": 1.50
}
```

---

### 2️⃣ Submit Loan Application (Maker Loan Officer)
Submits a new loan request for ETB 50,000.00 for member `+251955336000`.

- **URL**: `POST http://localhost:8085/api/v1/loan-org/apply`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <TELLER_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8085/api/v1/loan-org/apply \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <TELLER_TOKEN>" \
  -d '{
    "userId": "<MEMBER_USER_ID>",
    "productId": "<PRODUCT_ID>",
    "scoringType": "INDIVIDUAL",
    "amountRequested": 50000.00,
    "savingsConsistency": 95.00,
    "historicalYield": 12000.00,
    "projectedYield": 18000.00,
    "landSizeHectares": 2.50,
    "collaterals": []
  }'
```

**JSON Payload:**
```json
{
  "userId": "<MEMBER_USER_ID>",
  "productId": "<PRODUCT_ID>",
  "scoringType": "INDIVIDUAL",
  "amountRequested": 50000.00,
  "savingsConsistency": 95.00,
  "historicalYield": 12000.00,
  "projectedYield": 18000.00,
  "landSizeHectares": 2.50,
  "collaterals": []
}
```

**Expected Response (201 Created):**
Save `applicationId` (e.g. `b4c...-uuid`) for approval step.

---

### 3️⃣ Approve Loan Application (Maker-Checker Review)
Supervising admin approves loan application. Status transitions to `APPROVED`.

- **URL**: `PATCH http://localhost:8085/api/v1/loan-org/approve/<APPLICATION_ID>`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X PATCH http://localhost:8085/api/v1/loan-org/approve/<APPLICATION_ID> \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>" \
  -d '{
    "actionType": "APPROVE",
    "amountApproved": 50000.00,
    "remarks": "Credit score and savings collateral verified. Approved."
  }'
```

**JSON Payload:**
```json
{
  "actionType": "APPROVE",
  "amountApproved": 50000.00,
  "remarks": "Credit score and savings collateral verified. Approved."
}
```

---

### 4️⃣ Query Member Loan Applications
Lists loan applications submitted by member `<MEMBER_USER_ID>`.

- **URL**: `GET http://localhost:8085/api/v1/loan-org/applications/user/<MEMBER_USER_ID>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8085/api/v1/loan-org/applications/user/<MEMBER_USER_ID> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```
