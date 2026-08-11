# 🏦 Account Management Service (`account-management-service`) API Test Suite

This document contains step-by-step cURL commands and JSON payloads to test core ledger account creation, Luhn account number generation, Four-Eye Maker-Checker account activation, and account audit logs.

---

## 📱 Test Actors & Roles Used

| Role | Actor | Target SACCO | Phone Number |
|---|---|---|---|
| **SACCO_ADMIN** | Awach SACCO Admin (Checker) | Awach SACCO (`sacco_awach_sacco`) | `+251911109512` |
| **TELLER / EMPLOYEE** | Awach SACCO Teller (Maker) | Awach SACCO (`sacco_awach_sacco`) | `+251961741038` |
| **PRIMARY MEMBER** | Abebe Bikila (Member) | Awach SACCO (`sacco_awach_sacco`) | `+251955336000` |

---

## 🧪 Sequential API Test Execution Flow

### 1️⃣ Open Member Account (Maker Teller)
Teller opens a new savings account for member `+251955336000`. Generates Luhn account number. Status: `PENDING_APPROVAL`.

- **URL**: `POST http://localhost:8082/api/v1/accounts`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <TELLER_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8082/api/v1/accounts \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <TELLER_TOKEN>" \
  -d '{
    "userId": "<MEMBER_USER_ID>",
    "saccoCode": "AS001",
    "branchCode": "001",
    "productCode": "SAV01"
  }'
```

**JSON Payload:**
```json
{
  "userId": "<MEMBER_USER_ID>",
  "saccoCode": "AS001",
  "branchCode": "001",
  "productCode": "SAV01"
}
```

**Expected Response (201 Created):**
Save `accountNo` (e.g. `AS001-001-SAV01-0000016`) for subsequent steps:
```json
{
  "success": true,
  "data": {
    "accountNo": "AS001-001-SAV01-0000016",
    "userId": "<MEMBER_USER_ID>",
    "status": "PENDING_APPROVAL",
    "bookBalance": 0.00,
    "availableBalance": 0.00
  },
  "message": "Member account opened successfully. Status: PENDING_APPROVAL"
}
```

---

### 2️⃣ Approve Account Opening (Checker Admin)
Supervising admin approves account, activating status from `PENDING_APPROVAL` to `ACTIVE`. Triggers SMS notification to `+251955336000`.

- **URL**: `PUT http://localhost:8082/api/v1/accounts/<ACCOUNT_NO>/approve`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X PUT http://localhost:8082/api/v1/accounts/<ACCOUNT_NO>/approve \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```

---

### 3️⃣ Query Account Details by Account Number
Retrieves account ledger balances and active status.

- **URL**: `GET http://localhost:8082/api/v1/accounts/<ACCOUNT_NO>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8082/api/v1/accounts/<ACCOUNT_NO> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```

---

### 4️⃣ Query Accounts by Phone Number
Tenant-isolated lookup returning accounts linked to phone `+251955336000`.

- **URL**: `GET http://localhost:8082/api/v1/accounts/phone/%2B251955336000`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET "http://localhost:8082/api/v1/accounts/phone/%2B251955336000" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```

---

### 5️⃣ Query Account Audit Logs
Inspects immutable state change records for account `<ACCOUNT_NO>`.

- **URL**: `GET http://localhost:8082/api/v1/account-mgmt/audit/account/<ACCOUNT_NO>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8082/api/v1/account-mgmt/audit/account/<ACCOUNT_NO> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```
