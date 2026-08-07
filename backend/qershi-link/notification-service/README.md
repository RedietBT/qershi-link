# Qershi Link Backend - Notification & Messaging Service

A core multi-tenant microservice built using **Spring Boot 3.4.2 (Java 21)** following strict **Clean Architecture / Hexagonal Architecture** patterns. This service serves as the central messaging engine across the SACCO platform, handling direct SMS, multi-language template rendering (Amharic, English, Afaan Oromo, Tigrinya), third-party SMS vendor integration (AfroMessage / Ethio Telecom SMS Gateway), dynamic SMS provider adapter resolution, and delivery audit trail logging.

---

## 🏛️ Architecture & Key Features

### 1. Multi-Tenant Physical Schema Isolation
- Employs PostgreSQL **schema-per-tenant** routing dynamically via `TenantContext` (ThreadLocal), `TenantIdentifierResolver`, and `PostgresSchemaConnectionProvider`.
- Stores message templates and delivery audit logs in isolated SACCO tenant schemas (`sacco_xxx.notification_templates`, `sacco_xxx.notification_logs`).

### 2. Vendor-Agnostic SMS Provider Strategy Pattern
- Uses `NotificationProviderPort` outbound interface with pluggable provider adapters (`AfroMessageSmsAdapter`, `GenericWebhookSmsAdapter`).
- `NotificationProviderFactory` dynamically resolves the active provider adapter from property `${notification.provider.default}` or incoming request overrides without touching core code.

### 3. Inter-Service gRPC Server Integration (Port `9086`)
- Exposes `NotificationGrpcService` (`SendSmsNotification`) for high-speed sub-millisecond binary RPC dispatches from `transaction-service`, `loan-service`, and `identity-auth-service`. Supports gRPC tenant schema propagation.

### 4. Core API Surface (HTTP Port 8086 | gRPC Port 9086)
- **SMS Dispatching** (`POST /api/v1/notifications/sms/send`)
- **Delivery Audit Logs** (`GET /api/v1/notifications/logs`, `GET /api/v1/notifications/logs/recipient/{phone}`)
- **Template Management** (`POST /api/v1/notifications/templates`, `GET /api/v1/notifications/templates`, `GET /api/v1/notifications/templates/{code}`, `PUT /api/v1/notifications/templates/{code}`)

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
./mvnw clean package -pl notification-service -am -DskipTests
```

### Step 2: Build the Container Image
Build the container image targeting the service directory:
```powershell
docker build -t notification-service:latest ./notification-service
```

### Step 3: Rolling Update to Kubernetes
Apply a rolling restart sequence inside the `sacco-core` namespace:
```powershell
kubectl rollout restart deployment/notification-service -n sacco-core
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

*(Use the **"Select a Spec"** dropdown menu at the top right of the screen to switch between `5. Notification & Messaging Service`, `1. Identity & Auth Service`, `2. Member Profile Service`, `3. Account Management Service`, and `4. Transaction Management Service`).*

---

## 🪵 Troubleshooting & Diagnostic Runbooks

### Stream Live Logs
```powershell
kubectl logs -f deployment/notification-service -n sacco-core
```

### Inspect Historical Lifecycles (The Crash Log)
```powershell
kubectl logs deployment/notification-service -n sacco-core --previous
```

### Track Live Pod Infrastructure Events
```powershell
kubectl describe pod -n sacco-core <pod-name-from-get-pods>
```
