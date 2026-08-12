# Qershi Link - Multi-Tenant Notification End-to-End Test Guide

This guide provides step-by-step cURL commands and JSON payloads to test the Qershi Link multi-tenant notification system using `@` placeholder syntax (e.g. `@Name_of_the_user`, `@SACCO_NAME`, `@AMOUNT`, `@balance`).

---

## 1. How Template Placeholder Filling Works

SACCO Admins define custom templates **ONCE** using `@` placeholders. The system automatically fills in these placeholders with real-time transaction data when an action occurs.

### Supported Placeholders:
| Placeholder | Description | System Parameter Name |
| :--- | :--- | :--- |
| `@Name_of_the_user` / `@memberName` | Full name of the member/user | `memberName` |
| `@SACCO_NAME` / `@saccoName` | Official name of the SACCO | `saccoName` |
| `@AMOUNT` / `@Amount` | Transaction or loan amount | `amount` |
| `@balance` / `@Balance` | Updated account balance | `balance` |
| `@accountNo` | Member bank/savings account number | `accountNo` |
| `@productName` | Savings product type (e.g. Regular Savings) | `productName` |
| `@receiverName` | Recipient name for transfers | `receiverName` |
| `@receiverAccountNo` | Recipient account number for transfers | `receiverAccountNo` |
| `@loanId` | Active loan identifier | `loanId` |
| `@remainingBalance` | Remaining loan balance | `remainingBalance` |

---

## Step 1: Register Super Admin (Platform Administration)
Registers a new platform-wide Super Admin. Sends an SMS with the initial security PIN (`OTP_CODE`).

- **HTTP Method**: `POST`
- **URL**: `http://localhost:8080/api/v1/platform/register-admin`
- **Headers**:
  ```http
  Content-Type: application/json
  ```
- **Request Body**:
  ```json
  {
    "msisdn": "+251947990257"
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8080/api/v1/platform/register-admin' \
    -H 'Content-Type: application/json' \
    -d '{
      "msisdn": "+251947990257"
    }'
  ```
- **Expected SMS Delivery**:
  `"Welcome to System Platform! Your Super Admin PIN is: <PIN>"`

---

## Step 2: Onboard New SACCO & Register SACCO Admin
Onboards a new SACCO tenant, provisions database schema (`sacco_awash_savings_sacco`), seeds default templates, and registers the SACCO Admin.

- **HTTP Method**: `POST`
- **URL**: `http://localhost:8080/api/v1/sacco/onboard`
- **Headers**:
  ```http
  Content-Type: application/json
  ```
- **Request Body**:
  ```json
  {
    "saccoName": "Awash Savings SACCO",
    "adminName": "Dawit Tefera",
    "adminMsisdn": "+251911223344",
    "adminEmail": "dawit@awashsacco.com",
    "region": "Oromia",
    "licenseNumber": "AWS-2026-001",
    "isUnion": false,
    "minShareRequirement": 500.0
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8080/api/v1/sacco/onboard' \
    -H 'Content-Type: application/json' \
    -d '{
      "saccoName": "Awash Savings SACCO",
      "adminName": "Dawit Tefera",
      "adminMsisdn": "+251911223344",
      "adminEmail": "dawit@awashsacco.com",
      "region": "Oromia",
      "licenseNumber": "AWS-2026-001",
      "isUnion": false,
      "minShareRequirement": 500.0
    }'
  ```
- **Expected SMS Delivery**:
  `"Welcome to Qershi Link! Your admin account for Awash Savings SACCO is ready. Use PIN: <PIN> to log in. Please change your PIN immediately after your first login."`

---

## Step 3: Login as SACCO Admin & Obtain JWT Access Token
Authenticates the SACCO Admin using phone number and received PIN to obtain the JWT token for tenant management.

- **HTTP Method**: `POST`
- **URL**: `http://localhost:8080/api/v1/auth/login`
- **Headers**:
  ```http
  Content-Type: application/json
  ```
- **Request Body**:
  ```json
  {
    "msisdn": "+251911223344",
    "pin": "<PIN_FROM_STEP_2>"
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8080/api/v1/auth/login' \
    -H 'Content-Type: application/json' \
    -d '{
      "msisdn": "+251911223344",
      "pin": "<PIN_FROM_STEP_2>"
    }'
  ```

---

## Step 4: Customize SACCO Notification Template Using `@` Placeholders
Updates the text for template `CASH_DEPOSIT_ALERT` in the tenant schema `sacco_awash_savings_sacco` using `@Name_of_the_user`, `@SACCO_NAME`, `@AMOUNT`, `@balance`.

- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8086/api/v1/notifications/templates/CASH_DEPOSIT_ALERT?content=Dear%20@Name_of_the_user,%20your%20account%20@accountNo%20at%20@SACCO_NAME%20has%20been%20credited%20with%20@AMOUNT%20ETB.%20Your%20current%20balance%20is%20@balance%20ETB.%20Thanks%20for%20using%20our%20service.&active=true`
- **Headers**:
  ```http
  Authorization: Bearer <JWT_TOKEN_FROM_STEP_3>
  X-Tenant-Schema: sacco_awash_savings_sacco
  ```
- **cURL Command**:
  ```bash
  curl -X PUT 'http://localhost:8086/api/v1/notifications/templates/CASH_DEPOSIT_ALERT?content=Dear%20@Name_of_the_user,%20your%20account%20@accountNo%20at%20@SACCO_NAME%20has%20been%20credited%20with%20@AMOUNT%20ETB.%20Your%20current%20balance%20is%20@balance%20ETB.%20Thanks%20for%20using%20our%20service.&active=true' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```

---

## Step 5: Test Notification Dispatch Payloads for All Platform Actions

Dispatches test notifications using `POST http://localhost:8086/api/v1/notifications/sms/send`.

### 1. Account Opening Notification (`ACCOUNT_OPENED_ALERT`)
- **Template Code**: `ACCOUNT_OPENED_ALERT`
- **JSON Payload**:
  ```json
  {
    "recipientPhone": "+251911223344",
    "templateCode": "ACCOUNT_OPENED_ALERT",
    "parameters": {
      "memberName": "Dawit Tefera",
      "saccoName": "Awash Savings SACCO",
      "productName": "Regular Savings",
      "accountNo": "AWS-10002001"
    }
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8086/api/v1/notifications/sms/send' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco' \
    -H 'Content-Type: application/json' \
    -d '{
      "recipientPhone": "+251911223344",
      "templateCode": "ACCOUNT_OPENED_ALERT",
      "parameters": {
        "memberName": "Dawit Tefera",
        "saccoName": "Awash Savings SACCO",
        "productName": "Regular Savings",
        "accountNo": "AWS-10002001"
      }
    }'
  ```

---

### 2. Cash Deposit Notification (`CASH_DEPOSIT_ALERT`)
- **Template Code**: `CASH_DEPOSIT_ALERT`
- **JSON Payload**:
  ```json
  {
    "recipientPhone": "+251911223344",
    "templateCode": "CASH_DEPOSIT_ALERT",
    "parameters": {
      "memberName": "Abebe Bikila",
      "saccoName": "Awash Savings SACCO",
      "amount": "5000",
      "accountNo": "AWS-10001001",
      "balance": "15000"
    }
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8086/api/v1/notifications/sms/send' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco' \
    -H 'Content-Type: application/json' \
    -d '{
      "recipientPhone": "+251911223344",
      "templateCode": "CASH_DEPOSIT_ALERT",
      "parameters": {
        "memberName": "Abebe Bikila",
        "saccoName": "Awash Savings SACCO",
        "amount": "5000",
        "accountNo": "AWS-10001001",
        "balance": "15000"
      }
    }'
  ```
- **Resulting Rendered SMS Message**:
  `"Dear Abebe Bikila, your account AWS-10001001 at Awash Savings SACCO has been credited with 5000 ETB. Your current balance is 15000 ETB. Thanks for using our service."`

---

### 3. Cash Withdrawal Notification (`CASH_WITHDRAWAL_ALERT`)
- **Template Code**: `CASH_WITHDRAWAL_ALERT`
- **JSON Payload**:
  ```json
  {
    "recipientPhone": "+251911223344",
    "templateCode": "CASH_WITHDRAWAL_ALERT",
    "parameters": {
      "memberName": "Abebe Bikila",
      "saccoName": "Awash Savings SACCO",
      "amount": "2000",
      "accountNo": "AWS-10001001",
      "balance": "13000"
    }
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8086/api/v1/notifications/sms/send' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco' \
    -H 'Content-Type: application/json' \
    -d '{
      "recipientPhone": "+251911223344",
      "templateCode": "CASH_WITHDRAWAL_ALERT",
      "parameters": {
        "memberName": "Abebe Bikila",
        "saccoName": "Awash Savings SACCO",
        "amount": "2000",
        "accountNo": "AWS-10001001",
        "balance": "13000"
      }
    }'
  ```

---

### 4. Transfer Sent Notification (`TRANSFER_SENT_ALERT`)
- **Template Code**: `TRANSFER_SENT_ALERT`
- **JSON Payload**:
  ```json
  {
    "recipientPhone": "+251911223344",
    "templateCode": "TRANSFER_SENT_ALERT",
    "parameters": {
      "memberName": "Abebe Bikila",
      "saccoName": "Awash Savings SACCO",
      "amount": "1500",
      "receiverName": "Tigist Alemu",
      "receiverAccountNo": "AWS-10003002",
      "balance": "11500"
    }
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8086/api/v1/notifications/sms/send' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco' \
    -H 'Content-Type: application/json' \
    -d '{
      "recipientPhone": "+251911223344",
      "templateCode": "TRANSFER_SENT_ALERT",
      "parameters": {
        "memberName": "Abebe Bikila",
        "saccoName": "Awash Savings SACCO",
        "amount": "1500",
        "receiverName": "Tigist Alemu",
        "receiverAccountNo": "AWS-10003002",
        "balance": "11500"
      }
    }'
  ```

---

### 5. Loan Application Approved (`LOAN_APPLICATION_APPROVED`)
- **Template Code**: `LOAN_APPLICATION_APPROVED`
- **JSON Payload**:
  ```json
  {
    "recipientPhone": "+251911223344",
    "templateCode": "LOAN_APPLICATION_APPROVED",
    "parameters": {
      "memberName": "Tigist Alemu",
      "saccoName": "Awash Savings SACCO",
      "amount": "50000"
    }
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8086/api/v1/notifications/sms/send' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco' \
    -H 'Content-Type: application/json' \
    -d '{
      "recipientPhone": "+251911223344",
      "templateCode": "LOAN_APPLICATION_APPROVED",
      "parameters": {
        "memberName": "Tigist Alemu",
        "saccoName": "Awash Savings SACCO",
        "amount": "50000"
      }
    }'
  ```

---

### 6. Loan Amount Disbursed (`LOAN_DISBURSED`)
- **Template Code**: `LOAN_DISBURSED`
- **JSON Payload**:
  ```json
  {
    "recipientPhone": "+251911223344",
    "templateCode": "LOAN_DISBURSED",
    "parameters": {
      "memberName": "Tigist Alemu",
      "saccoName": "Awash Savings SACCO",
      "amount": "50000"
    }
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8086/api/v1/notifications/sms/send' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco' \
    -H 'Content-Type: application/json' \
    -d '{
      "recipientPhone": "+251911223344",
      "templateCode": "LOAN_DISBURSED",
      "parameters": {
        "memberName": "Tigist Alemu",
        "saccoName": "Awash Savings SACCO",
        "amount": "50000"
      }
    }'
  ```

---

### 7. Loan Repayment Confirmation (`LOAN_REPAYMENT_CONFIRMATION`)
- **Template Code**: `LOAN_REPAYMENT_CONFIRMATION`
- **JSON Payload**:
  ```json
  {
    "recipientPhone": "+251911223344",
    "templateCode": "LOAN_REPAYMENT_CONFIRMATION",
    "parameters": {
      "memberName": "Tigist Alemu",
      "saccoName": "Awash Savings SACCO",
      "amount": "5000",
      "loanId": "LN-2026-0042",
      "remainingBalance": "45000"
    }
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8086/api/v1/notifications/sms/send' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco' \
    -H 'Content-Type: application/json' \
    -d '{
      "recipientPhone": "+251911223344",
      "templateCode": "LOAN_REPAYMENT_CONFIRMATION",
      "parameters": {
        "memberName": "Tigist Alemu",
        "saccoName": "Awash Savings SACCO",
        "amount": "5000",
        "loanId": "LN-2026-0042",
        "remainingBalance": "45000"
      }
    }'
  ```

---

## Step 6: Inspect SMS Delivery Audit Logs
Retrieves the complete notification delivery audit trail for the active tenant schema.

- **HTTP Method**: `GET`
- **URL**: `http://localhost:8086/api/v1/notifications/logs`
- **Headers**:
  ```http
  Authorization: Bearer <JWT_TOKEN>
  X-Tenant-Schema: sacco_awash_savings_sacco
  ```
- **cURL Command**:
  ```bash
  curl -X GET 'http://localhost:8086/api/v1/notifications/logs' \
    -H 'Authorization: Bearer <JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```
