# Qershi Link - Enterprise Cloud-Native Core Banking Platform

A high-performance, multi-tenant enterprise **Core Banking System (CBS)** engineered with **Java 21**, **Spring Boot 3.4.2**, **gRPC**, and **PostgreSQL** under strict **Clean / Hexagonal Architecture** principles. Designed to meet global financial institution standards (aligned with **Oracle FLEXCUBE, Temenos Transact, Finacle, and Mambu**), the platform powers full-lifecycle retail banking, corporate banking, loan origination, post-origination servicing, double-entry general ledger accounting, and multi-channel transaction processing.

---

## 🏛️ Enterprise Architecture Overview

The system utilizes a modern microservices architecture with a **physical schema-per-tenant isolation model**, ensuring strict data segregation, regulatory compliance, and high scale across financial entities while maintaining centralized infrastructure management.

```
                                    +-----------------------+
                                    |   swagger-api-hub     |
                                    |      (Port 8090)      |
                                    +-----------+-----------+
                                                |
            ┌───────────────────────────────────┼───────────────────────────────────┐
            │                                   │                                   │
            ▼                                   ▼                                   ▼
+───────────────────────+           +───────────────────────+           +───────────────────────+
| identity-auth-service |           |    profile-service    |           |account-mgmt-service   |
| (HTTP 8080 / gRPC 9080)           | (HTTP 8081 / gRPC 9081)           | (HTTP 8082 / gRPC 9082)           |
+───────────┬───────────+           +───────────┬───────────+           +───────────┬───────────+
            │                                   │                                   │
            ├───────────────────────────────────┼───────────────────────────────────┤
            │                      High-Speed gRPC Binary Stream                    │
            ├───────────────────────────────────┼───────────────────────────────────┤
            │                                   │                                   │
            ▼                                   ▼                                   ▼
+───────────────────────+           +───────────────────────+           +───────────────────────+
|transaction-mgmt-serv. |           | loan-origination-serv |           | loan-management-serv  |
| (HTTP 8083 / gRPC 9083)           | (HTTP 8084 / gRPC 9084)           | (HTTP 8085 / gRPC 9085)           |
+───────────┬───────────+           +───────────┬───────────+           +───────────┬───────────+
            │                                   │                                   │
            └───────────────────────────────────┼───────────────────────────────────┘
                                                │
                                                ▼
                                    +───────────────────────+
                                    | notification-service  |
                                    | (HTTP 8086 / gRPC 9086|
                                    +-----------------------+
```

---

## 📦 Core Microservices Ecosystem

| Microservice | HTTP Port | gRPC Port | Primary Banking Capabilities |
|---|---|---|---|
| 🌐 **`swagger-api-hub`** | `8090` | N/A | Centralized API Documentation Hub & Microservice OpenAPI Proxy Gateway |
| 🔑 **`identity-auth-service`** | `8080` | `9080` | Multi-Tenant Authentication, OAuth2/JWT Issuance, Tenant Onboarding, Dynamic RBAC |
| 👤 **`profile-service`** | `8081` | `9081` | Customer Demographics, KYC Verification, Address, Next of Kin, Employment Management |
| 💳 **`account-management-service`** | `8082` | `9082` | Multi-Currency Savings Accounts, Term Deposits, Checking Accounts, Lifecycle States |
| 💸 **`transaction-management-service`** | `8083` | `9083` | Double-Entry General Ledger, Over-The-Counter Cash Postings, Inter-Account Transfers |
| 📋 **`loan-origination-service` (LOS)** | `8084` | `9084` | Credit Scoring, Dynamic Maker-Checker Approval Workflows, Group Borrowing Units |
| 🏦 **`loan-management-service` (LMS)** | `8085` | `9085` | Multi-Strategy Amortization Engine, Tier-1 Payment Waterfall Engine, Delinquency Tracking |
| 📲 **`notification-service`** | `8086` | `9086` | Event-Driven SMS & Email Alerts, Template Engines, Multi-Provider Fallback Dispatch |
| 🌐 **`payment-gateway-service`** | N/A | N/A | Third-Party Payment Gateway Integrations & External Interbank Clearing Orchestration |
| 🧰 **`common-library`** | N/A | N/A | Shared Core Banking DTOs, Context Interceptors, PII Masking Utilities (`PiiMasker`) |

---

## ⚡ Core Banking Feature Highlights

### 1. Dynamic Tier-1 Financial Product Configuration
Aligned with international standards (**Oracle FLEXCUBE, Temenos Transact, Finacle & Mambu**), financial parameters are decoupled from hardcoded enums and dynamically driven via database configurations:
- **Repayment Frequencies**: `MONTHLY`, `QUARTERLY`, `SEMI_ANNUAL`, `ANNUAL`, `BULLET`.
- **Interest Calculation Strategies**: `REDUCING_BALANCE`, `FLAT_RATE`, `COMPOUND`, `TIERED`.
- **Payment Channels**: `SAVINGS_ACCOUNT`, `CASH`, `MOBILE_MONEY`, `WIRE_TRANSFER`.

### 2. Pure Domain Amortization Calculation Engine
Calculates precise financial schedules with compounding options:
- **Reducing Balance Amortization (EMI Formula)**:
  $$EMI = P \times r \times \frac{(1+r)^n}{(1+r)^n - 1}$$
- **Bullet Amortization**: Single principal balloon payment at maturity with accrued interest settlement.

### 3. Payment Waterfall Allocation Engine
Strict Tier-1 Core Banking repayment distribution sequence for overdue and current installments:
$$\text{1. Late Fees \& Penalties} \longrightarrow \text{2. Accrued Interest Due} \longrightarrow \text{3. Principal Balance}$$

### 4. Enterprise Security & Multi-Tenancy
- **Physical Schema Isolation**: Per-thread dynamic PostgreSQL schema resolution (`SET search_path TO {tenant_schema}, public`).
- **Dynamic RBAC & Maker-Checker**: Fine-grained resource action permissions coupled with enterprise administrator roles (`ADMIN`).
- **PII Protection**: Automatic PII sanitization in log outputs via `PiiMasker`.

---

## 🛠️ Technology Stack

- **Core Framework**: Java 21 LTS, Spring Boot 3.4.2
- **Inter-Service Communication**: gRPC (HTTP/2 binary protocol) & Protocol Buffers (`.proto`)
- **Persistence & Migration**: Spring Data JPA, Hibernate 6 ORM, Flyway DDL Migrations, PostgreSQL 16+
- **Security & Docs**: Spring Security, JJWT (JWT), Springdoc OpenAPI 3.0, Swagger UI
- **Containerization & Orchestration**: Docker, Docker Compose, Kubernetes (`kubectl`)

---

## 🚀 Deployment & Local Setup Guide

### 1. Prerequisites
- **JDK 21** & **Maven 3.9+**
- **Docker Desktop** with **Kubernetes** enabled
- **PostgreSQL 16+**

### 2. Build All Services
```bash
cd backend/qershi-link
./mvnw clean package -DskipTests
```

### 3. Build Docker Container Images
```bash
docker build -t identity-auth-service:latest -f backend/qershi-link/identity-auth-service/Dockerfile backend/qershi-link/
docker build -t profile-service:latest -f backend/qershi-link/profile-service/Dockerfile backend/qershi-link/
docker build -t account-management-service:latest -f backend/qershi-link/account-management-service/Dockerfile backend/qershi-link/
docker build -t transaction-management-service:latest -f backend/qershi-link/transaction-management-service/Dockerfile backend/qershi-link/
docker build -t loan-origination-service:latest -f backend/qershi-link/loan-origination-service/Dockerfile backend/qershi-link/
docker build -t loan-management-service:latest -f backend/qershi-link/loan-management-service/Dockerfile backend/qershi-link/
docker build -t notification-service:latest -f backend/qershi-link/notification-service/Dockerfile backend/qershi-link/
docker build -t swagger-api-hub:latest -f backend/qershi-link/swagger-api-hub/Dockerfile backend/qershi-link/
```

### 4. Deploy to Kubernetes Cluster
```bash
kubectl apply -f backend/qershi-link/deployments/
```

### 5. Access Interactive Centralized Swagger API Hub
Once the cluster is running, open your browser:
👉 **`http://localhost:8090/swagger-ui/index.html`**

Access all 7 core banking microservice APIs dynamically using the top-right service selector.

---

## 📄 License & Attribution

Developed by **KAB Digital Solution PLC**. Enterprise Core Banking Platform for high-availability cloud deployments.
