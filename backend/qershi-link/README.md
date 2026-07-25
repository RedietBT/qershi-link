# Qershi Link Backend - Identity & Authentication Service

A core multi-tenant microservice built using **Spring Boot 3.4.2 (Java 21)** following strict **Clean Architecture / Hexagonal Architecture** patterns. This service orchestrates master identity data platforms while programmatically provisioning isolated physical schema namespaces per tenant (SACCO/Union) under a resilient Zero-Orphan database isolation policy.

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
./mvnw clean package



### Step 2: Build the Container Image

Build the container layer using the network host bridge. Target the root project level and map to the target Dockerfile profile:

```powershell
docker build --network=host -t identity-auth-service:latest -f identity-auth-service/Dockerfile .

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

## 🔍 Accessing Live Swagger Documentation

Because the application boots inside an isolated virtual Kubernetes network namespace, you must establish an interface bridge from your host operating system to view the live API schema.

### Expose Port Globally (Persistent Tunnel Command)

To open a permanent port tunnel that remains open in the background without locking your current terminal session, execute:

```powershell
Start-Job -ScriptBlock { kubectl port-forward deployment/identity-auth-service 8080:8080 -n sacco-core }

```

*(Alternatively, you can run `kubectl port-forward deployment/identity-auth-service 8080:8080 -n sacco-core` in a separate PowerShell window and keep it open).*

### Open the API Interactive Console

Once the tunnel is up, you and your team can bypass the Spring Security parameter and run payloads directly via the active link below:

👉 **[http://localhost:8080/swagger-ui/index.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui/index.html)**

---

## 🪵 Troubleshooting & Diagnostic Runbooks

If your deployment slips into a `CrashLoopBackOff` status or signals an initialization `Error`, use these exact debugging commands to surface stack traces:

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