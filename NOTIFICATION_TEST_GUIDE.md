# Qershi Link - SACCO Notification Template Customization & Test Guide

This guide demonstrates how SACCO Admins configure custom message templates using `@` placeholders (without needing to supply parameter values when creating/updating templates), and how the platform automatically connects them to service actions.

---

## 1. SACCO Admin Template Management Concept

1. **SACCO Admin Action**: The SACCO Admin writes custom message text using `@` placeholders for each system action.
2. **Storage**: The template is stored in the SACCO's isolated database schema (`sacco_<name>.notification_templates`).
3. **Automated System Trigger**: When a transaction, loan approval, or account opening occurs, the system automatically fetches the SACCO's custom template, extracts real-time database values, and replaces the `@` placeholders before sending the SMS.

---

## 2. Complete Catalog of SACCO Custom Template Configuration APIs

SACCO Admins configure custom templates for each action using:
`PUT http://localhost:8086/api/v1/notifications/templates/{templateCode}`

Headers required:
```http
Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>
X-Tenant-Schema: sacco_awash_savings_sacco
```

---

### Action 1: Account Opening Notification (`ACCOUNT_OPENED_ALERT`)

#### SACCO Custom Message Template Wording:
> `"Welcome to @SACCO_NAME! Dear @Name_of_the_user, your @productName account @accountNo has been successfully opened. Thank you for choosing our SACCO!"`

#### Update Template API Request:
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8086/api/v1/notifications/templates/ACCOUNT_OPENED_ALERT?content=Welcome%20to%20@SACCO_NAME!%20Dear%20@Name_of_the_user,%20your%20@productName%20account%20@accountNo%20has%20been%20successfully%20opened.%20Thank%20you%20for%20choosing%20our%20SACCO!&active=true`
- **cURL Command**:
  ```bash
  curl -X PUT 'http://localhost:8086/api/v1/notifications/templates/ACCOUNT_OPENED_ALERT?content=Welcome%20to%20@SACCO_NAME!%20Dear%20@Name_of_the_user,%20your%20@productName%20account%20@accountNo%20has%20been%20successfully%20opened.%20Thank%20you%20for%20choosing%20our%20SACCO!&active=true' \
    -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```

---

### Action 2: Cash Deposit Notification (`CASH_DEPOSIT_ALERT`)

#### SACCO Custom Message Template Wording:
> `"Dear @Name_of_the_user, your account @accountNo at @SACCO_NAME has been credited with @AMOUNT ETB. Your current balance is @balance ETB. Thanks for your service."`

#### Update Template API Request:
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8086/api/v1/notifications/templates/CASH_DEPOSIT_ALERT?content=Dear%20@Name_of_the_user,%20your%20account%20@accountNo%20at%20@SACCO_NAME%20has%20been%20credited%20with%20@AMOUNT%20ETB.%20Your%20current%20balance%20is%20@balance%20ETB.%20Thanks%20for%20your%20service.&active=true`
- **cURL Command**:
  ```bash
  curl -X PUT 'http://localhost:8086/api/v1/notifications/templates/CASH_DEPOSIT_ALERT?content=Dear%20@Name_of_the_user,%20your%20account%20@accountNo%20at%20@SACCO_NAME%20has%20been%20credited%20with%20@AMOUNT%20ETB.%20Your%20current%20balance%20is%20@balance%20ETB.%20Thanks%20for%20your%20service.&active=true' \
    -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```

---

### Action 3: Cash Withdrawal Notification (`CASH_WITHDRAWAL_ALERT`)

#### SACCO Custom Message Template Wording:
> `"Dear @Name_of_the_user, @AMOUNT ETB has been withdrawn from your account @accountNo at @SACCO_NAME. Your remaining balance is @balance ETB."`

#### Update Template API Request:
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8086/api/v1/notifications/templates/CASH_WITHDRAWAL_ALERT?content=Dear%20@Name_of_the_user,%20@AMOUNT%20ETB%20has%20been%20withdrawn%20from%20your%20account%20@accountNo%20at%20@SACCO_NAME.%20Your%20remaining%20balance%20is%20@balance%20ETB.&active=true`
- **cURL Command**:
  ```bash
  curl -X PUT 'http://localhost:8086/api/v1/notifications/templates/CASH_WITHDRAWAL_ALERT?content=Dear%20@Name_of_the_user,%20@AMOUNT%20ETB%20has%20been%20withdrawn%20from%20your%20account%20@accountNo%20at%20@SACCO_NAME.%20Your%20remaining%20balance%20is%20@balance%20ETB.&active=true' \
    -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```

---

### Action 4: Transfer Sent Notification (`TRANSFER_SENT_ALERT`)

#### SACCO Custom Message Template Wording:
> `"Dear @Name_of_the_user, you have transferred @AMOUNT ETB to @receiverName (@receiverAccountNo) via @SACCO_NAME. Your new balance is @balance ETB."`

#### Update Template API Request:
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8086/api/v1/notifications/templates/TRANSFER_SENT_ALERT?content=Dear%20@Name_of_the_user,%20you%20have%20transferred%20@AMOUNT%20ETB%20to%20@receiverName%20(@receiverAccountNo)%20via%20@SACCO_NAME.%20Your%20new%20balance%20is%20@balance%20ETB.&active=true`
- **cURL Command**:
  ```bash
  curl -X PUT 'http://localhost:8086/api/v1/notifications/templates/TRANSFER_SENT_ALERT?content=Dear%20@Name_of_the_user,%20you%20have%20transferred%20@AMOUNT%20ETB%20to%20@receiverName%20(@receiverAccountNo)%20via%20@SACCO_NAME.%20Your%20new%20balance%20is%20@balance%20ETB.&active=true' \
    -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```

---

### Action 5: Loan Application Approval (`LOAN_APPLICATION_APPROVED`)

#### SACCO Custom Message Template Wording:
> `"Congratulations @Name_of_the_user! Your loan application for @AMOUNT ETB has been APPROVED by @SACCO_NAME credit committee."`

#### Update Template API Request:
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8086/api/v1/notifications/templates/LOAN_APPLICATION_APPROVED?content=Congratulations%20@Name_of_the_user!%20Your%20loan%20application%20for%20@AMOUNT%20ETB%20has%20been%20APPROVED%20by%20@SACCO_NAME%20credit%20committee.&active=true`
- **cURL Command**:
  ```bash
  curl -X PUT 'http://localhost:8086/api/v1/notifications/templates/LOAN_APPLICATION_APPROVED?content=Congratulations%20@Name_of_the_user!%20Your%20loan%20application%20for%20@AMOUNT%20ETB%20has%20been%20APPROVED%20by%20@SACCO_NAME%20credit%20committee.&active=true' \
    -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```

---

### Action 6: Loan Amount Disbursed (`LOAN_DISBURSED`)

#### SACCO Custom Message Template Wording:
> `"Dear @Name_of_the_user, your approved loan of @AMOUNT ETB from @SACCO_NAME has been successfully DISBURSED to your savings account."`

#### Update Template API Request:
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8086/api/v1/notifications/templates/LOAN_DISBURSED?content=Dear%20@Name_of_the_user,%20your%20approved%20loan%20of%20@AMOUNT%20ETB%20from%20@SACCO_NAME%20has%20been%20successfully%20DISBURSED%20to%20your%20savings%20account.&active=true`
- **cURL Command**:
  ```bash
  curl -X PUT 'http://localhost:8086/api/v1/notifications/templates/LOAN_DISBURSED?content=Dear%20@Name_of_the_user,%20your%20approved%20loan%20of%20@AMOUNT%20ETB%20from%20@SACCO_NAME%20has%20been%20successfully%20DISBURSED%20to%20your%20savings%20account.&active=true' \
    -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```

---

### Action 7: Loan Repayment Confirmation (`LOAN_REPAYMENT_CONFIRMATION`)

#### SACCO Custom Message Template Wording:
> `"Dear @Name_of_the_user, repayment of @AMOUNT ETB received for loan @loanId at @SACCO_NAME. Remaining loan balance: @remainingBalance ETB. Thank you!"`

#### Update Template API Request:
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8086/api/v1/notifications/templates/LOAN_REPAYMENT_CONFIRMATION?content=Dear%20@Name_of_the_user,%20repayment%20of%20@AMOUNT%20ETB%20received%20for%20loan%20@loanId%20at%20@SACCO_NAME.%20Remaining%20loan%20balance:%20@remainingBalance%20ETB.%20Thank%20you!&active=true`
- **cURL Command**:
  ```bash
  curl -X PUT 'http://localhost:8086/api/v1/notifications/templates/LOAN_REPAYMENT_CONFIRMATION?content=Dear%20@Name_of_the_user,%20repayment%20of%20@AMOUNT%20ETB%20received%20for%20loan%20@loanId%20at%20@SACCO_NAME.%20Remaining%20loan%20balance:%20@remainingBalance%20ETB.%20Thank%20you!&active=true' \
    -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
  ```

---

## 3. How the System Tests Dispatch & Placeholder Substitution

Once the SACCO Admin saves any of the custom templates above, you can test dispatching an alert using `POST http://localhost:8086/api/v1/notifications/sms/send`.

### Test Cash Deposit Dispatch (`CASH_DEPOSIT_ALERT`)
- **JSON Body**:
  ```json
  {
    "recipientPhone": "+251947990257",
    "templateCode": "CASH_DEPOSIT_ALERT",
    "parameters": {
      "memberName": "Abebe Bikila",
      "saccoName": "Awash Savings SACCO",
      "accountNo": "AWS-10001001",
      "amount": "5000",
      "balance": "15000"
    }
  }
  ```
- **cURL Command**:
  ```bash
  curl -X POST 'http://localhost:8086/api/v1/notifications/sms/send' \
    -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
    -H 'X-Tenant-Schema: sacco_awash_savings_sacco' \
    -H 'Content-Type: application/json' \
    -d '{
      "recipientPhone": "+251947990257",
      "templateCode": "CASH_DEPOSIT_ALERT",
      "parameters": {
        "memberName": "Abebe Bikila",
        "saccoName": "Awash Savings SACCO",
        "accountNo": "AWS-10001001",
        "amount": "5000",
        "balance": "15000"
      }
    }'
  ```
- **Resulting Rendered SMS Output**:
  `"Dear Abebe Bikila, your account AWS-10001001 at Awash Savings SACCO has been credited with 5000 ETB. Your current balance is 15000 ETB. Thanks for your service."`

---

## 4. Inspecting Audit Logs

Retrieves complete SMS delivery log for the tenant schema:
```bash
curl -X GET 'http://localhost:8086/api/v1/notifications/logs' \
  -H 'Authorization: Bearer <SACCO_ADMIN_JWT_TOKEN>' \
  -H 'X-Tenant-Schema: sacco_awash_savings_sacco'
```
