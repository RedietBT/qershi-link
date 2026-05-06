# 🚀 Qershi-Link SACCO Digitization
**Company:** KAB Digital Solution PLC  
**Lead Developer:** Arsema Degu Addis

This project is a microservices-based SACCO management system built strictly with **Spring Boot** and **PostgreSQL**.

## 🏗 System Architecture
We follow **Clean Architecture** and **MVC** patterns. Please organize your code as follows:
*   **Domain**: Business entities and core logic.
*   **Application**: Service layers and Handlers (must include input validation).
*   **Infrastructure**: Database schemas, JPA repositories, and external configs.

## 🗄 Database Strategy
*   **Multi-tenant**: We use a schema-per-tenant strategy.
*   **Auth Requirement**: When a user is deleted from the auth service, the corresponding profile **MUST** be deleted from the `profile_schema.profile` table.

## 🏁 How to Run the Project
### 1. Prerequisites
*   Install **Docker Desktop** and enable the built-in **Kubernetes** cluster.
*   Install **Java 21**.

### 2. Prepare the Cluster
```powershell
kubectl create namespace sacco-core
```

### 3. Launch the Infrastructure
```powershell
kubectl apply -f deployments/postgres-db.yaml
```

### 4. Build and Deploy Services
From the root directory (`qershi-link-parent`):
```powershell
# Build the image locally
docker build -t identity-auth-service:latest -f identity-auth-service/Dockerfile .

# Deploy to Kubernetes
kubectl apply -f deployments/identity-service.yaml
```

---

## 🔍 Verification (The "Lead Dev" Checklist)
After deploying, use these steps to ensure everything is working correctly:

### 1. Check Pod Status
Ensure both `identity-auth-service` and `postgres-db` show `1/1 READY` and `Running`:
```powershell
kubectl get pods -n sacco-core
```

### 2. Verify Database Connection
Check the application logs to ensure Spring Boot successfully connected to PostgreSQL:
```powershell
kubectl logs -l app=identity-auth -n sacco-core
```
*Look for: `HikariPool-1 - Start completed.`*

### 3. Inspect the Database
If you need to check the `profile_schema.profile` table manually:
```powershell
kubectl exec -it <postgres-pod-name> -n sacco-core -- psql -U postgres -d qershi-link
```

### 4. Access Swagger UI
To test the API handlers and validation:
1. Start port-forwarding:
   ```powershell
   kubectl port-forward service/identity-auth-service 8080:80 -n sacco-core
   ```
2. Open: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## ⚠️ Important Rules
*   **Image Pull Policy**: All YAML files use `imagePullPolicy: Never`. You **must** build locally first.
*   **Validation**: Every Handler must use `@Valid` or `@Validated` as configured in the Parent POM.
*   **Git Hygiene**: Do not push `target/`, `.idea/`, or `.env` files. Ensure the `.gitignore` is active.

---

### Why this extra info helps your friends:
*   **Independence**: They can fix their own `ErrImagePull` or database connection issues without asking you.
*   **Consistency**: They will use the same `kubectl` commands you used, ensuring the whole team sees the same results.
*   **Transparency**: It explains *why* we use `imagePullPolicy: Never`, which is a common point of confusion for new Kubernetes users.
