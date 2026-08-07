# Qershi Link Backend - Transaction Management Service

A core multi-tenant microservice built using **Spring Boot 3.4.2 (Java 21)** following strict **Clean Architecture / Hexagonal Architecture** patterns. This service orchestrates over-the-counter teller cash deposits, withdrawals, member-to-member internal transfers, idempotency key protections, and double-entry General Ledger (GL) posting entries (`DEBIT` & `CREDIT` lines).

---

## 🏛️ Architecture & Key Features

### 1. Multi-Tenant Schema Isolation
- Employs PostgreSQL **schema-per-tenant** routing dynamically via `TenantContext` (ThreadLocal), `TenantIdentifierResolver`, and `PostgresSchemaConnectionProvider`.
- Stores customer-facing transactions and General Ledger journal entries in isolated SACCO tenant schemas (`sacco_xxx.transactions`, `sacco_xxx.journal_entries`, `sacco_xxx.journal_lines`, `sacco_xxx.transaction_audit_logs`).

### 2. Double-Entry General Ledger (GL) Accounting Engine
- Automatic balanced GL Journal Entry creation for every completed transaction:
  - **Teller Cash Deposit**: `DEBIT 1010-TELLER-VAULT-CASH` | `CREDIT 2010-MEMBER-SAVINGS-{accNo}`
  - **Teller Cash Withdrawal**: `DEBIT 2010-MEMBER-SAVINGS-{accNo}` | `CREDIT 1010-TELLER-VAULT-CASH`
  - **Member Transfer**: `DEBIT 2010-MEMBER-SAVINGS-{senderAcc}` | `CREDIT 2010-MEMBER-SAVINGS-{receiverAcc}`

### 3. Inter-Service gRPC Integration (Client & Server)
- **gRPC Client**: Calls `account-management-service` on port `9082` (`AccountGrpcService`) to execute real-time credit/debit validations and available balance verification before posting transactions. Passes `tenant_schema` in gRPC requests.
- **gRPC Server (Port `9083`)**: Exposes transaction reflection and health status.

### 4. Core API Surface (HTTP Port 8083 | gRPC Port 9083)
- **Teller Cash Operations** (`POST /api/v1/transactions/deposit`, `POST /api/v1/transactions/withdraw`)
- **Internal Member Transfers** (`POST /api/v1/transactions/transfer`)
- **Transaction & GL Journal Inquiries** (`GET /api/v1/transactions/account/{accountNo}`, `GET /api/v1/transactions/{transactionRef}`, `GET /api/v1/transactions/{transactionRef}/journal`)

---

## 🚀 Prerequisites

Before launching the cluster, ensure your local development workstation has the following installed:
* **Operating System:** Windows 10/11 with PowerShell
* **Java Development Kit (JDK):** Version 21
* **Containerization:** Docker Desktop with Kubernetes enabled
* **Orchestration Client:** `kubectl` CLI

---

## 🛠️ How to Run the Project

Follow these sequential steps to compile your code, update your cluster images, and deploy safely to your infrastructure namespace.

### Step 1: Package the Microservice
Compile and package the source application into an executable fat JAR:
```powershell
./mvnw clean package -pl transaction-management-service -am -DskipTests
```

### Step 2: Build the Container Image
Build the container image targeting the service directory:
```powershell
docker build -t transaction-management-service:latest ./transaction-management-service
```

### Step 3: Rolling Update to Kubernetes
Apply a rolling restart sequence inside the `sacco-core` namespace:
```powershell
kubectl rollout restart deployment/transaction-management-service -n sacco-core
```

### Step 4: Monitor Pod Synchronization
Watch real-time pod status transitions:
```powershell
kubectl get pods -n sacco-core -w
```

---

## 🌐 Accessing Centralized Swagger Documentation Hub (Port 8090)

The Centralized Swagger API Hub runs as a Kubernetes `LoadBalancer` service directly accessible on port `8090`:

👉 **http://localhost:8090/swagger-ui.html**

*(Use the **"Select a Spec"** dropdown menu at the top right of the screen to switch between `4. Transaction Management Service`, `1. Identity & Auth Service`, `2. Member Profile Service`, `3. Account Management Service`, and other microservices).*

---

## 🪵 Troubleshooting & Diagnostic Runbooks

### Stream Live Logs
```powershell
kubectl logs -f deployment/transaction-management-service -n sacco-core
```

### Inspect Historical Lifecycles (The Crash Log)
```powershell
kubectl logs deployment/transaction-management-service -n sacco-core --previous
```

### Track Live Pod Infrastructure Events
```powershell
kubectl describe pod -n sacco-core <pod-name-from-get-pods>
```
