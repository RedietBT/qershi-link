# 🔐 Identity & Authentication Service (`identity-auth-service`) API Test Suite

This document contains step-by-step cURL commands and JSON payloads to thoroughly test the functionality of `identity-auth-service` for **Qershi Link Core Banking**.

---

## 📱 Test Actor & Phone Number Assignments

The following 5 verified phone numbers are assigned to distinct system actors to prevent duplicate MSISDN collisions while testing multi-tenant isolation:

| Role | Actor Context | Target SACCO | Phone Number | Initial Temporary PIN | New Verified PIN |
|---|---|---|---|---|---|
| **SUPER_ADMIN** | Global Platform Administrator | System Master | `+251947990257` | N/A | `984215` |
| **SACCO_ADMIN (SACCO 1)** | Awach SACCO Admin | Awach SACCO (`sacco_awach_sacco`) | `+251911109512` | `883492` | `749201` |
| **TELLER / EMPLOYEE** | Awach SACCO Teller / Officer | Awach SACCO (`sacco_awach_sacco`) | `+251961741038` | `448912` | `615283` |
| **PRIMARY MEMBER** | Member (Deposits, Loans & SMS) | Awach SACCO (`sacco_awach_sacco`) | `+251955336000` | `119482` | `839214` |
| **SACCO_ADMIN (SACCO 2)** | Tsehay SACCO Admin | Tsehay SACCO (`sacco_tsehay_sacco`) | `+251995220266` | `552194` | `482915` |

---

## 🧹 Database Reset Script (Fresh Wipe)

Run the following SQL commands in your PostgreSQL client (`qershi-link` database) to wipe out previous test data while preserving master permissions:

```sql
-- 1. Truncate master identity & security tables
TRUNCATE TABLE master_schema.refresh_tokens CASCADE;
TRUNCATE TABLE master_schema.audit_logs CASCADE;
DELETE FROM master_schema.users;
DELETE FROM master_schema.sacco_registry WHERE sacco_id != '00000000-0000-0000-0000-000000000000';

-- 2. Drop sample tenant schemas if they exist
DROP SCHEMA IF EXISTS sacco_awach_sacco CASCADE;
DROP SCHEMA IF EXISTS sacco_tsehay_sacco CASCADE;
```

---

## 🧪 Sequential API Test Execution Flow

### 1️⃣ Register Global SUPER_ADMIN
Creates the top-level platform administrator account.

- **URL**: `POST http://localhost:8080/api/v1/auth/super-admin/register`
- **Headers**: `Content-Type: application/json`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/super-admin/register \
  -H "Content-Type: application/json" \
  -d '{
    "msisdn": "+251947990257",
    "pin": "984215"
  }'
```

**JSON Payload:**
```json
{
  "msisdn": "+251947990257",
  "pin": "984215"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "userId": "e7b...-uuid",
    "msisdn": "+251947990257",
    "globalRole": "SUPER_ADMIN",
    "status": "ACTIVE"
  },
  "message": "Super Admin registered successfully."
}
```

---

### 2️⃣ Login as SUPER_ADMIN & Extract JWT Token
Authenticates the `SUPER_ADMIN` to get an authorization token for SACCO onboarding.

- **URL**: `POST http://localhost:8080/api/v1/auth/login`
- **Headers**: `Content-Type: application/json`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "msisdn": "+251947990257",
    "pin": "984215"
  }'
```

**JSON Payload:**
```json
{
  "msisdn": "+251947990257",
  "pin": "984215"
}
```

**Expected Response (200 OK):**
Save the `accessToken` returned below for `SUPER_ADMIN_TOKEN`:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userContext": {
    "userId": "e7b...",
    "saccoId": "00000000-0000-0000-0000-000000000000",
    "schemaName": "master_schema",
    "role": "SUPER_ADMIN",
    "permissions": ["PLATFORM_MANAGE_SACCO", "USER_VIEW_ALL"]
  }
}
```

---

### 3️⃣ Onboard SACCO 1 (Awach SACCO)
Provisions tenant schema `sacco_awach_sacco` and registers its SACCO Admin (`+251911109512`).

- **URL**: `POST http://localhost:8080/api/v1/saccos/onboard`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <SUPER_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/saccos/onboard \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SUPER_ADMIN_TOKEN>" \
  -d '{
    "saccoName": "Awach SACCO",
    "schemaPrefix": "awach_sacco",
    "minShareRequirement": 1000.00,
    "adminMsisdn": "+251911109512",
    "adminInitialPin": "883492"
  }'
```

**JSON Payload:**
```json
{
  "saccoName": "Awach SACCO",
  "schemaPrefix": "awach_sacco",
  "minShareRequirement": 1000.00,
  "adminMsisdn": "+251911109512",
  "adminInitialPin": "883492"
}
```

---

### 4️⃣ Onboard SACCO 2 (Tsehay SACCO - Multi-Tenant Isolation Sample)
Provisions second tenant schema `sacco_tsehay_sacco` and registers its SACCO Admin (`+251995220266`).

- **URL**: `POST http://localhost:8080/api/v1/saccos/onboard`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <SUPER_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/saccos/onboard \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SUPER_ADMIN_TOKEN>" \
  -d '{
    "saccoName": "Tsehay SACCO",
    "schemaPrefix": "tsehay_sacco",
    "minShareRequirement": 500.00,
    "adminMsisdn": "+251995220266",
    "adminInitialPin": "552194"
  }'
```

**JSON Payload:**
```json
{
  "saccoName": "Tsehay SACCO",
  "schemaPrefix": "tsehay_sacco",
  "minShareRequirement": 500.00,
  "adminMsisdn": "+251995220266",
  "adminInitialPin": "552194"
}
```

---

### 5️⃣ First-Time Login for SACCO 1 Admin (Expect `PENDING_PASSWORD`)
Verifies mandatory PIN rotation enforcement on initial credential usage.

- **URL**: `POST http://localhost:8080/api/v1/auth/login`
- **Headers**: `Content-Type: application/json`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "msisdn": "+251911109512",
    "pin": "883492"
  }'
```

**Expected Response (200 OK):**
```json
{
  "accessToken": null,
  "tokenType": null,
  "expiresIn": 0,
  "userContext": {
    "userId": "a1b...",
    "saccoId": "c2d...",
    "schemaName": "sacco_awach_sacco",
    "role": "PENDING_PASSWORD",
    "permissions": []
  }
}
```

---

### 6️⃣ Rotate PIN for SACCO 1 Admin
Enforces Core Banking PIN complexity rules (must not be trivial `123456` or match old PIN).

- **URL**: `POST http://localhost:8080/api/v1/auth/change-pin`
- **Headers**: `Content-Type: application/json`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-pin \
  -H "Content-Type: application/json" \
  -d '{
    "msisdn": "+251911109512",
    "oldPin": "883492",
    "newPin": "749201"
  }'
```

**JSON Payload:**
```json
{
  "msisdn": "+251911109512",
  "oldPin": "883492",
  "newPin": "749201"
}
```

---

### 7️⃣ Re-Login as SACCO 1 Admin & Save Token
Obtains `SACCO1_ADMIN_TOKEN` with full administrative privileges.

- **URL**: `POST http://localhost:8080/api/v1/auth/login`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "msisdn": "+251911109512",
    "pin": "749201"
  }'
```

---

### 8️⃣ Register Teller / Employee for SACCO 1
Creates an operational teller account under Awach SACCO (`+251961741038`).

- **URL**: `POST http://localhost:8080/api/v1/users/register`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>" \
  -d '{
    "msisdn": "+251961741038",
    "initialPin": "448912",
    "globalRole": "TELLER"
  }'
```

**JSON Payload:**
```json
{
  "msisdn": "+251961741038",
  "initialPin": "448912",
  "globalRole": "TELLER"
}
```

---

### 9️⃣ Register Primary Member for SACCO 1
Creates the main member user account (`+251955336000`) used for account opening, cash deposits, and loans.

- **URL**: `POST http://localhost:8080/api/v1/users/register`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>" \
  -d '{
    "msisdn": "+251955336000",
    "initialPin": "119482",
    "globalRole": "MEMBER"
  }'
```

**JSON Payload:**
```json
{
  "msisdn": "+251955336000",
  "initialPin": "119482",
  "globalRole": "MEMBER"
}
```

---

### 🔟 Test PIN Complexity Guard (Expect Rejection)
Verifies that predictable PINs (like `123456` or `111111`) are rejected.

- **URL**: `POST http://localhost:8080/api/v1/auth/change-pin`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-pin \
  -H "Content-Type: application/json" \
  -d '{
    "msisdn": "+251955336000",
    "oldPin": "119482",
    "newPin": "123456"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "PIN complexity violation: PIN '123456' is too trivial."
}
```

---

### 1️⃣1️⃣ Logout & JWT Revocation (Redis Blacklist)
Revokes the token so subsequent requests with the same token are rejected with 401 Unauthorized.

- **URL**: `POST http://localhost:8080/api/v1/auth/logout`
- **Headers**: 
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```

---

### 1️⃣2️⃣ Query Authentication Security Audit Logs

#### A. Global Platform Logs (as SUPER_ADMIN)
- **URL**: `GET http://localhost:8080/api/v1/platform/audit-logs`
- **Headers**: `Authorization: Bearer <SUPER_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8080/api/v1/platform/audit-logs \
  -H "Authorization: Bearer <SUPER_ADMIN_TOKEN>"
```

#### B. Tenant-Scoped Auth Logs (as SACCO 1 ADMIN)
- **URL**: `GET http://localhost:8080/api/v1/platform/audit-logs/tenant`
- **Headers**: `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8080/api/v1/platform/audit-logs/tenant \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```
