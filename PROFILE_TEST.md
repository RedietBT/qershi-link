# 👤 Profile Service (`profile-service`) API Test Suite

This document contains step-by-step cURL commands and JSON payloads to test member profile creation, PII demographics, address/employment updates, and Maker-Checker onboarding approval in `profile-service`.

---

## 📱 Test Actors & Roles Used

| Role | Actor | Target SACCO | Phone Number |
|---|---|---|---|
| **SACCO_ADMIN** | Awach SACCO Administrator | Awach SACCO (`sacco_awach_sacco`) | `+251911109512` |
| **PRIMARY MEMBER** | Abebe Bikila (Member) | Awach SACCO (`sacco_awach_sacco`) | `+251955336000` |

---

## 🧪 Sequential API Test Execution Flow

### 1️⃣ Create Member Profile (Demographics)
Registers initial PII demographics for primary member (`+251955336000`). Status defaults to `PENDING_APPROVAL`.

- **URL**: `POST http://localhost:8081/api/v1/profiles`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8081/api/v1/profiles \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>" \
  -d '{
    "userId": "<MEMBER_USER_ID>",
    "firstName": "Abebe",
    "middleName": "Bikila",
    "lastName": "Tessema",
    "gender": "MALE",
    "dateOfBirth": "1990-05-15",
    "maritalStatus": "MARRIED"
  }'
```

**JSON Payload:**
```json
{
  "userId": "<MEMBER_USER_ID>",
  "firstName": "Abebe",
  "middleName": "Bikila",
  "lastName": "Tessema",
  "gender": "MALE",
  "dateOfBirth": "1990-05-15",
  "maritalStatus": "MARRIED"
}
```

---

### 2️⃣ Save Contact Address
Attaches primary phone number (`+251955336000`) and residential address to member profile.

- **URL**: `POST http://localhost:8081/api/v1/profiles/<MEMBER_USER_ID>/address`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8081/api/v1/profiles/<MEMBER_USER_ID>/address \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>" \
  -d '{
    "primaryPhone": "+251955336000",
    "secondaryPhone": "+251947990257",
    "email": "abebe.bikila@qershi.com",
    "region": "Addis Ababa",
    "zoneSubcity": "Bole Subcity",
    "woreda": "Woreda 03",
    "houseNumber": "House 1045"
  }'
```

---

### 3️⃣ Save Employment Profile
Attaches employment, employer name, and monthly income details.

- **URL**: `POST http://localhost:8081/api/v1/profiles/<MEMBER_USER_ID>/employment`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X POST http://localhost:8081/api/v1/profiles/<MEMBER_USER_ID>/employment \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>" \
  -d '{
    "occupationSector": "FINANCE",
    "employerName": "Awach Sacco Commercial Department",
    "monthlyIncome": 35000.00,
    "tinNumber": "TIN-99201482",
    "employeeId": "EMP-2026-0041"
  }'
```

---

### 4️⃣ Approve Member Onboarding (Maker-Checker Activation)
Supervising admin approves member profile, changing status from `PENDING_APPROVAL` to `ACTIVE`.

- **URL**: `PUT http://localhost:8081/api/v1/profiles/<MEMBER_USER_ID>/approve`
- **Headers**: 
  - `Content-Type: application/json`
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

**cURL Request:**
```bash
curl -X PUT http://localhost:8081/api/v1/profiles/<MEMBER_USER_ID>/approve \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>" \
  -d '{
    "remarks": "KYC documents verified and approved for full SACCO membership."
  }'
```

---

### 5️⃣ Query Member Profile Details
Retrieves aggregate profile details including address, employment, and status.

- **URL**: `GET http://localhost:8081/api/v1/profiles/<MEMBER_USER_ID>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8081/api/v1/profiles/<MEMBER_USER_ID> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```

---

### 6️⃣ Query Profile Audit Logs (Compliance Audit Trail)
Retrieves immutable profile modification audit logs for member `+251955336000`.

- **URL**: `GET http://localhost:8081/api/v1/profiles/audit/user/<MEMBER_USER_ID>`
- **Headers**: 
  - `X-Tenant-ID: sacco_awach_sacco`
  - `Authorization: Bearer <SACCO1_ADMIN_TOKEN>`

```bash
curl -X GET http://localhost:8081/api/v1/profiles/audit/user/<MEMBER_USER_ID> \
  -H "X-Tenant-ID: sacco_awach_sacco" \
  -H "Authorization: Bearer <SACCO1_ADMIN_TOKEN>"
```
