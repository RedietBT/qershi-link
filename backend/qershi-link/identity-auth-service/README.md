# Qershi Link Backend - Identity & Authentication Service

A core multi-tenant microservice built using **Spring Boot 3.4.2 (Java 21)** following strict **Clean Architecture / Hexagonal Architecture** patterns. This service orchestrates master identity data platforms while programmatically provisioning isolated physical schema namespaces per tenant (SACCO/Union) under a resilient Zero-Orphan database isolation policy.

---

## 🏛️ Architecture & Key Features

### 1. Multi-Tenant Schema Isolation & Tenant Provisioning
- Uses PostgreSQL **schema-per-tenant** routing dynamically via `TenantContext` (ThreadLocal), `TenantIdentifierResolver`, and `PostgresSchemaConnectionProvider`.
- Platform registries and global user credentials live in `master_schema`.
- Onboarding a new SACCO automatically provisions an isolated PostgreSQL schema with independent tenant tables (`roles`, `permissions`, `role_permissions`, `user_roles`, `accounts`, `account_products`, `transactions`, `journal_entries`, `journal_lines`, and `transaction_audit_logs`).

### 2. Dual-Layer Security Model (RBAC + Global Roles)
- **Global Roles** (`SUPER_ADMIN`, `SACCO_ADMIN`, `UNION_ADMIN`, `SACCO_USER`, `TELLER`, `MEMBER`): Governs system-wide access control via Spring Security `@PreAuthorize("hasAnyRole(...)")`.
- **Tenant-Scoped Permissions**: Granular resource/action authorities (`MEMBER_CREATE`, `MEMBER_APPROVE`, `CASH_DEPOSIT`, `SAVINGS_WITHDRAW`, `TRANSACTION_TRANSFER`, `TRANSACTION_VIEW`, `USER_VIEW_ALL`, etc.) assigned to tenant roles within isolated SACCO namespaces.

### 3. Core API Surface (HTTP Port 8080)
- **Public Entrypoints** (`/api/v1/auth/login`, `/api/v1/platform/register-admin`, `/api/v1/sacco/onboard`)
- **Authentication Engine** (`POST /api/v1/auth/login`, `POST /api/v1/auth/change-password`)
- **SACCO Onboarding & Management** (`POST /api/v1/sacco/onboard`, `GET /api/v1/saccos`, `GET /api/v1/saccos/{id}`)
- **User Account Management** (`GET /api/v1/users`, `POST /api/v1/users`, `GET /api/v1/users/{id}`, `PUT /api/v1/users/{id}`, `DELETE /api/v1/users/{id}`, `POST /api/v1/users/{userId}/roles/{roleId}`)
- **RBAC Management** (`GET /api/v1/roles/permissions`, `POST /api/v1/roles`)

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
Compile and package the source application into an executable fat JAR running framework verification checks:
```powershell
./mvnw clean package -pl identity-auth-service -am -DskipTests
```

### Step 2: Build the Container Image
Build the container layer targeting the Dockerfile:
```powershell
docker build -t identity-auth-service:latest ./identity-auth-service
```

### Step 3: Rolling Update to Kubernetes
Apply a rolling restart sequence inside the `sacco-core` namespace:
```powershell
kubectl rollout restart deployment/identity-auth-service -n sacco-core
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

*(Use the **"Select a Spec"** dropdown menu at the top right of the screen to switch between `1. Identity & Auth Service`, `2. Member Profile Service`, `3. Account Management Service`, `4. Transaction Management Service`, and other microservices).*

---

## 🪵 Troubleshooting & Diagnostic Runbooks

### Stream Live Logs
```powershell
kubectl logs -f deployment/identity-auth-service -n sacco-core
```

### Inspect Historical Lifecycles (The Crash Log)
```powershell
kubectl logs deployment/identity-auth-service -n sacco-core --previous
```

### Track Live Pod Infrastructure Events
```powershell
kubectl describe pod -n sacco-core <pod-name-from-get-pods>
```
