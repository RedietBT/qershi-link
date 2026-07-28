# Qershi Link Backend - Identity & Authentication Service

A core multi-tenant microservice built using **Spring Boot 3.4.2 (Java 21)** following strict **Clean Architecture / Hexagonal Architecture** patterns. This service orchestrates master identity data platforms while programmatically provisioning isolated physical schema namespaces per tenant (SACCO/Union) under a resilient Zero-Orphan database isolation policy.

---

## 🏛️ Architecture & Key Features

### 1. Multi-Tenant Schema Isolation
- Uses PostgreSQL **schema-per-tenant** routing dynamically via `TenantContext` (ThreadLocal), `TenantIdentifierResolver`, and `PostgresSchemaConnectionProvider`.
- Platform registries and global user credentials live in `master_schema`.
- Onboarding a new SACCO automatically provisions an isolated PostgreSQL schema with independent `roles`, `permissions`, `role_permissions`, and `user_roles` tables.

### 2. Dual-Layer Security Model (RBAC + Global Roles)
- **Global Roles** (`SUPER_ADMIN`, `SACCO_ADMIN`, `UNION_ADMIN`, `SACCO_USER`, `TELLER`, `MEMBER`): Governs system-wide access control via Spring Security `@PreAuthorize("hasAnyRole(...)")`.
- **Tenant-Scoped Permissions** (`MEMBER_CREATE`, `LOAN_APPROVE`, `CASH_DEPOSIT`, `USER_VIEW_ALL`, etc.): Granular resource/action authorities assigned to tenant roles within isolated SACCO namespaces.

### 3. Core API Surface
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
./mvnw clean package -pl identity-auth-service -am
```

### Step 2: Build the Container Image

Build the container layer using the network host bridge. Target the root project level and map to the target Dockerfile profile:

```powershell
docker build -t identity-auth-service:latest -f identity-auth-service/Dockerfile .
```

### Step 3: Rolling Update to Kubernetes

Apply a rolling restart sequence to force your local cluster to pull the newly built image layer inside the dedicated namespace:

```powershell
kubectl rollout restart deployment/identity-auth-service -n sacco-core
```

### Step 4: Monitor Pod Synchronization

Watch the real-time status transitions as old container templates gracefully terminate and new ones spin up to an active state:

```powershell
kubectl get pods -n sacco-core -w
```

---

## 🌐 Accessing Centralized Swagger Documentation Hub (Port 8090)

The Centralized Swagger API Hub runs as a Kubernetes `LoadBalancer` service directly accessible on port `8090`:

👉 **http://localhost:8090/swagger-ui/index.html**

*(Use the **"Select a Spec"** dropdown menu at the top right of the screen to switch between `1. Identity & Auth Service`, `2. Member Profile Service`, and other microservices).*

---

## 🪵 Troubleshooting & Diagnostic Runbooks

If your deployment slips into a `CrashLoopBackOff` status or signals an initialization `Error`, use these exact debugging commands to surface stack traces:

### Stream Live Logs

To tail and follow live application logs from the running deployment:

```powershell
kubectl logs -f deployment/identity-auth-service -n sacco-core
```

### Inspect Historical Lifecycles (The Crash Log)

If a container crashes during context initialization, regular logging commands might return blank. Grab the exception trace from the *previously terminated* instance:

```powershell
kubectl logs deployment/identity-auth-service -n sacco-core --previous
```

### Track Live Pod Infrastructure Events

To inspect cluster scheduler actions, failing health/readiness probes, or underlying memory constraints, describe the active pod profile:

```powershell
kubectl describe pod -n sacco-core <pod-name-from-get-pods>
```
