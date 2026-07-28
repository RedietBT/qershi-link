# Qershi Link Backend - Core Microservices Platform

A high-performance, multi-tenant digital financial platform for SACCOs and Credit Unions built using **Java 21**, **Spring Boot 3.4.2**, **gRPC**, and **PostgreSQL** under strict **Clean / Hexagonal Architecture** principles.

---

## 🏛️ Architecture Overview

The system uses a **physical schema-per-tenant isolation model** to guarantee zero data leakage between onboarded SACCOs and Unions while maintaining system-wide security orchestration.

```
                                  +-----------------------+
                                  |    API Gateway /      |
                                  |    Ingress Route      |
                                  +-----------+-----------+
                                              |
                       ┌──────────────────────┴──────────────────────┐
                       │                                             │
                       ▼                                             ▼
          +──────────────────────────+                  +──────────────────────────+
          |  identity-auth-service   |                  |     profile-service      |
          |  (Port 8080 / gRPC Client) |                  | (Port 8081 / gRPC 9081)  |
          +────────────┬─────────────+                  +────────────┬─────────────+
                       │                                             │
                       │             gRPC Binary Stream              │
                       └─────────────────────────────────────────────┘
                                   (Port 9081 Cascade Delete)
```

---

## 📦 Microservices Matrix

| Microservice | HTTP Port | gRPC Port | Primary Purpose | README Link |
|---|---|---|---|---|
| **`identity-auth-service`** | `8080` | Client | Master Identity Platform, Authentication, SACCO Onboarding, RBAC Security | [identity-auth-service/README.md](file:///c:/Users/HP/Documents/projects/qershi-link/backend/qershi-link/identity-auth-service/README.md) |
| **`profile-service`** | `8081` | `9081` | SACCO Member Onboarding, Demographics, Address, Employment, KYC Verifications, Next of Kin | [profile-service/README.md](file:///c:/Users/HP/Documents/projects/qershi-link/backend/qershi-link/profile-service/README.md) |
| **`common-library`** | N/A | N/A | Shared Domain DTOs, Context Interceptors, PII Masking Utilities (`PiiMasker`) | [common-library/README.md](file:///c:/Users/HP/Documents/projects/qershi-link/backend/qershi-link/common-library) |

---

## 🚀 Prerequisites & System Requirements

* **Operating System:** Windows 10/11 with PowerShell
* **Java Development Kit (JDK):** Version 21
* **Build Tool:** Apache Maven (Wrapper included `./mvnw`)
* **Database Engine:** PostgreSQL 16+
* **Orchestration:** Docker Desktop with Kubernetes enabled & `kubectl` CLI

---

## 🛠️ Multi-Module Build & Deployment Runbook

### Step 1: Compile & Package All Microservices
```powershell
./mvnw clean package
```

### Step 2: Build Container Images
```powershell
# Build Identity Auth Service Image
docker build --network=host -t identity-auth-service:latest -f identity-auth-service/Dockerfile .

# Build Profile Service Image
docker build --network=host -t profile-service:latest -f profile-service/Dockerfile .
```

### Step 3: Deploy & Restart Kubernetes Cluster
```powershell
# Deploy Infrastructure Namespace
kubectl apply -f deployments/postgres-db.yaml
kubectl apply -f deployments/postgres-service.yaml

# Deploy Microservices
kubectl apply -f deployments/identity-service.yaml
kubectl apply -f deployments/profile-service.yaml

# Restart Deployments
kubectl rollout restart deployment/identity-auth-service -n sacco-core
kubectl rollout restart deployment/profile-service -n sacco-core
```

### Step 4: Monitor Pod Status
```powershell
kubectl get pods -n sacco-core -w
```

---

## 🔍 Swagger UI Interactive API Documentation

Establish persistent local port-forwarding tunnels to access interactive Swagger documentation consoles with custom Dark Mode toggle buttons:

| Service | Port Forwarding Command | Interactive Swagger UI URL |
|---|---|---|
| **Identity Auth Service** | `kubectl port-forward deployment/identity-auth-service 8080:8080 -n sacco-core` | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| **Profile Service** | `kubectl port-forward deployment/profile-service 8081:8081 -n sacco-core` | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) |

---

## 🔒 Security & Data Privacy Features

- **Multi-Tenant Physical Isolation**: Native PostgreSQL `SET search_path TO {tenant_schema}, public;` executed per thread context.
- **Dual-Layer Security Model**: Global system roles combined with tenant-scoped resource action permissions (`@PreAuthorize("hasAuthority(...)")`).
- **PII Protection**: Log outputs sanitized using `PiiMasker` to prevent leakage of phone numbers, government IDs, and email addresses.
- **Inter-Service Binary Communication**: Fast, persistent HTTP/2 gRPC channels between microservices.