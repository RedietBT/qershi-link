# Qershi Link Backend - Account Management Service

A core multi-tenant microservice built using **Spring Boot 3.4.2 (Java 21)** following strict **Clean Architecture / Hexagonal Architecture** patterns. This service manages SACCO deposit account products (Savings, Shares, Time Deposits), account lifecycle opening (`APP-SACCO-YYYY-SEQ`), balance tracking, freeze controls (`CREDIT_FREEZE`, `DEBIT_FREEZE`, `FULL_FREEZE`), and gRPC remote validation procedures.

---

## 🏛️ Architecture & Key Features

### 1. Multi-Tenant Physical Schema Isolation
- Employs PostgreSQL **schema-per-tenant** routing dynamically via `TenantContext` (ThreadLocal), `TenantIdentifierResolver`, and `PostgresSchemaConnectionProvider`.
- Stores financial deposit accounts and account products in isolated SACCO tenant schemas (`sacco_xxx.accounts`, `sacco_xxx.account_products`).

### 2. Inter-Service gRPC Integration (Server & Client)
- **gRPC Server (Port `9082`)**: Exposes `AccountGrpcService` (`GetAccountByNo`, `ValidateAccountForDebit`, `ValidateAccountForCredit`) to validate available balances and freeze controls for real-time transaction processing. Supports gRPC tenant schema propagation.
- **gRPC Client Adapter**: Calls `profile-service` on port `9081` for member verification checks during account opening.

### 3. Account Numbering & Freeze Controls
- **Standardized Account Number Format**: `[SACCO_CODE]-[BRANCH_CODE]-[PRODUCT_CODE]-[SEQUENCE]` (e.g., `0001-001-101-0000011`).
- **Freeze Status Enforcement**: Supports `NONE`, `CREDIT_FREEZE`, `DEBIT_FREEZE`, and `FULL_FREEZE` rules.

### 4. Core API Surface (HTTP Port 8082 | gRPC Port 9082)
- **Account Product Management** (`POST /api/v1/accounts/products`, `GET /api/v1/accounts/products`, `GET /api/v1/accounts/products/{productCode}`)
- **Deposit Account Opening & Management** (`POST /api/v1/accounts/open`, `GET /api/v1/accounts/{accountNo}`, `GET /api/v1/accounts/user/{userId}`, `GET /api/v1/accounts/phone/{phoneNumber}`, `PUT /api/v1/accounts/{accountNo}/freeze`)

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
./mvnw clean package -pl account-management-service -am -DskipTests
```

### Step 2: Build the Container Image
Build the container image targeting the service directory:
```powershell
docker build -t account-management-service:latest ./account-management-service
```

### Step 3: Rolling Update to Kubernetes
Apply a rolling restart sequence inside the `sacco-core` namespace:
```powershell
kubectl rollout restart deployment/account-management-service -n sacco-core
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

*(Use the **"Select a Spec"** dropdown menu at the top right of the screen to switch between `3. Account Management Service`, `1. Identity & Auth Service`, `2. Member Profile Service`, `4. Transaction Management Service`, and other microservices).*

---

## 🪵 Troubleshooting & Diagnostic Runbooks

### Stream Live Logs
```powershell
kubectl logs -f deployment/account-management-service -n sacco-core
```

### Inspect Historical Lifecycles (The Crash Log)
```powershell
kubectl logs deployment/account-management-service -n sacco-core --previous
```

### Track Live Pod Infrastructure Events
```powershell
kubectl describe pod -n sacco-core <pod-name-from-get-pods>
```
