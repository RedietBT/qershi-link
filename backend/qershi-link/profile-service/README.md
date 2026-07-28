# Qershi Link Backend - Member Profile Service

A core multi-tenant microservice built using **Spring Boot 3.4.2 (Java 21)** following strict **Clean Architecture / Hexagonal Architecture** patterns. This service manages SACCO member demographics, contact address handles, economic employment profiles, government ID KYC verifications, nominated beneficiary (Next of Kin) payout allocations, and Four-Eye Principle (Maker-Checker) onboarding approvals.

---

## 🏛️ Architecture & Key Features

### 1. Multi-Tenant Physical Schema Isolation
- Employs PostgreSQL **schema-per-tenant** routing dynamically via `TenantContext` (ThreadLocal), `TenantIdentifierResolver`, and `PostgresSchemaConnectionProvider`.
- Intercepts incoming `X-Tenant-ID` HTTP headers to set tenant context, executing native PostgreSQL `SET search_path TO {tenant_schema}, public;` statements on DB connections.

### 2. Inter-Service gRPC Server Integration
- Listens on gRPC Port `9081` (`ProfileGrpcService`) extending compiled Protocol Buffer stubs (`user_events.proto`).
- Exposes binary Remote Procedure Call `CascadeDeleteProfile` for `identity-auth-service` to trigger instant cascade deletion of member profiles, address, employment, KYC, and next-of-kin records when a user account is deleted.

### 3. Fine-Grained RBAC & PII Data Protection
- **Method-Level Security**: Secured via Spring Security `@PreAuthorize("hasAuthority('...')")` enforcing granular authorities (`MEMBER_CREATE`, `MEMBER_UPDATE`, `MEMBER_APPROVE`, `KYC_SUBMIT`, `KYC_VERIFY`, `NEXT_OF_KIN_MANAGE`).
- **PII Log Masking**: Includes `PiiMasker` log sanitization to mask sensitive phone numbers, emails, government IDs, and names in console/file logs.

### 4. Core API Surface (HTTP Port 8081)
- **Member Profile Lifecycle** (`POST /api/v1/profiles`, `PUT /api/v1/profiles/{userId}/demographics`, `POST /api/v1/profiles/{userId}/address`, `POST /api/v1/profiles/{userId}/employment`, `PUT /api/v1/profiles/{userId}/approve`, `PUT /api/v1/profiles/{userId}/status`, `GET /api/v1/profiles/{userId}`)
- **KYC Verification** (`POST /api/v1/kyc/{userId}/identifications`, `GET /api/v1/kyc/{userId}/identifications`, `PUT /api/v1/kyc/identifications/{id}/verify`, `PUT /api/v1/kyc/identifications/{id}/reject`)
- **Next of Kin Beneficiaries** (`POST /api/v1/kin/{userId}`, `GET /api/v1/kin/{userId}`, `PUT /api/v1/kin/{kinId}`, `DELETE /api/v1/kin/{kinId}`)

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
./mvnw clean package -pl profile-service -am
```

### Step 2: Build the Container Image

Build the container layer using the network host bridge. Target the root project level and map to the target Dockerfile profile:

```powershell
docker build --network=host -t profile-service:latest -f profile-service/Dockerfile .
```

### Step 3: Rolling Update to Kubernetes

Apply a rolling restart sequence to force your local cluster to pull the newly built image layer inside the dedicated namespace:

```powershell
kubectl rollout restart deployment/profile-service -n sacco-core
```

### Step 4: Monitor Pod Synchronization

Watch the real-time status transitions as old container templates gracefully terminate and new ones spin up to an active state:

```powershell
kubectl get pods -n sacco-core -w
```

---

## 🌐 Accessing Centralized Swagger Documentation Hub (Port 9020)

Instead of opening individual microservice Swagger ports, access all backend APIs through the **Centralized Swagger API Hub on Port 9020**:

### Expose Port 9020 Tunnel (Persistent Tunnel Command)

```powershell
Start-Job -ScriptBlock { kubectl port-forward deployment/swagger-api-hub 9020:9020 -n sacco-core }
```

### Open the Unified API Console

👉 **http://localhost:9020/swagger-ui.html**

*(Use the **"Select a Spec"** dropdown menu at the top right of the screen to switch between `2. Member Profile Service`, `1. Identity & Auth Service`, and other microservices).*

---

## 🪵 Troubleshooting & Diagnostic Runbooks

If your deployment slips into a `CrashLoopBackOff` status or signals an initialization `Error`, use these exact debugging commands to surface stack traces:

### Stream Live Logs

To tail and follow live application logs from the running deployment:

```powershell
kubectl logs -f deployment/profile-service -n sacco-core
```

### Inspect Historical Lifecycles (The Crash Log)

If a container crashes during context initialization, regular logging commands might return blank. Grab the exception trace from the *previously terminated* instance:

```powershell
kubectl logs deployment/profile-service -n sacco-core --previous
```

### Track Live Pod Infrastructure Events

To inspect cluster scheduler actions, failing health/readiness probes, or underlying memory constraints, describe the active pod profile:

```powershell
kubectl describe pod -n sacco-core <pod-name-from-get-pods>
```
