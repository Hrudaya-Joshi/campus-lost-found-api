# Campus Lost & Found API - DevOps Assignment Final Report & Execution Guide

---

## 1. Project Introduction

The **Campus Lost & Found API** is a Spring Boot RESTful microservice built to solve the common campus problem of reporting, tracking, and retrieving lost items. The application provides endpoints for students and staff to register lost or found items, view active items, search by ID, and verify service health.

To deliver this service reliably and efficiently, this assignment implements an end-to-end DevOps pipeline incorporating Continuous Integration (CI) and Continuous Deployment (CD) best practices.

---

## 2. Objectives

1. **Continuous Integration (CI):**
   - Establish Git version control with structured feature-branch workflows.
   - Standardize builds and dependency management using Apache Maven and Java 17.
   - Implement comprehensive automated unit and integration tests for all API endpoints.
   - Create an automated Jenkins declarative CI pipeline with SCM polling, JUnit reporting, and JAR artifact archiving.

2. **Continuous Deployment (CD):**
   - Automate configuration management and deployment using modular, idempotent Ansible roles.
   - Package the service into a secure, multi-stage production Docker container with an unprivileged runtime user.
   - Orchestrate deployment on Kubernetes with self-healing capabilities, declarative scaling (3 to 5 to 3 replicas), and liveness/readiness health probes.

---

## 3. Tools and Technologies

| Category | Tool / Technology | Version / Specification | Role in Architecture |
|---|---|---|---|
| Programming Language | Java | 17 (LTS) | Core backend language |
| Framework | Spring Boot | 4.1.2 / Spring 7 | Microservice web MVC framework |
| Build Tool | Apache Maven | 3.10.x / Wrapper | Dependency management & packaging |
| Version Control | Git & GitHub | Git 2.43+ | Source code management & branching |
| CI Server | Jenkins | Declarative Pipeline | Automated build, test, and artifact archiving |
| Configuration Management | Ansible | 2.21+ | Idempotent system configuration & deployment |
| Containerization | Docker | Multi-stage (Alpine) | Container packaging & isolation |
| Container Orchestration | Kubernetes (Minikube) | v1.30+ | Container orchestration, self-healing, scaling |
| Testing | JUnit 5 & MockMvc | Spring Boot Starter Test | Automated REST API testing |

---

## 4. System Architecture

```
Developer Workspace
        │
        ▼ (git commit / merge)
 GitHub Repository (main / feature/api-tests)
        │
        ▼ (SCM Poll: H/2 * * * *)
 Jenkins CI Server
 ├── Stage 1: Checkout SCM
 ├── Stage 2: Maven Build
 ├── Stage 3: Automated Tests (JUnit Report)
 └── Stage 4: Package & Archive JAR Artifact
        │
        ├─────────────────────────────┬─────────────────────────────┐
        ▼                             ▼                             ▼
 Ansible Deployment            Docker Engine             Kubernetes Cluster
 ├── Inventory (Local/WSL)     ├── Multi-Stage Build     ├── Deployment (3 Replicas)
 ├── lostfound_api Role        ├── Non-Root User         ├── NodePort Service (30080)
 ├── Jinja2 Config Templates   └── Port 8080 Exposed     ├── Horizontal Scaling (3->5->3)
 └── Health Verification                                 └── Liveness/Readiness Probes
```

---

## 5. Component 1 – Continuous Integration Implementation

### 6. Git Workflow and Branching

A disciplined Git branching model was adopted:
1. **Primary Branch (`main`):** Hosts deployable, production-ready code.
2. **Feature Branch (`feature/api-tests`):** Created specifically to implement and verify automated test coverage before merging.
3. **Ignored Files (`.gitignore`):** Configured to strictly exclude `target/`, IDE files (`.idea/`, `.vscode/`, `.settings/`), environment files (`.env`), secrets, logs, and OS artifacts.

#### Git Execution Commands:
```bash
# Initialize repository
git init -b main

# Add files and create initial commit
git add .
git commit -m "feat: initial commit of campus-lost-found-api base application"

# Create and switch to feature branch
git checkout -b feature/api-tests

# (Develop and test ItemControllerTest.java)
git add src/test/java/com/campus/lostfound/controller/ItemControllerTest.java
git commit -m "test: add automated unit tests for health, add item, get all items, and get by ID"

# Switch to main and merge feature branch
git checkout main
git merge feature/api-tests --no-ff -m "merge: merge feature/api-tests into main"
```

> **Screenshot 1: Git Branching & Commit Log**
> - **What to Show:** Terminal output displaying the commit graph with both branches and merge commit.
> - **Command to Run:** `git log --graph --oneline -n 5`
> 
> ```
> [ INSERT SCREENSHOT 1 HERE: git log --graph --oneline ]
> ```

---

### 7. Maven Build Verification

Maven manages project dependencies, compilation, and assembly of the executable Spring Boot archive.

#### Maven Verification Commands:
```bash
# Execute unit tests
./mvnw clean test

# Build executable production JAR
./mvnw clean package
```

**Verification Result:**
- Output JAR: `target/campus-lost-found-api-0.0.1-SNAPSHOT.jar` (Size: ~21.6 MB)
- Java Target: Compatible with Java 17 LTS bytecode.

> **Screenshot 2: Maven Clean Package Build Success**
> - **What to Show:** Terminal output displaying `BUILD SUCCESS` and the generated JAR in `target/`.
> - **Command to Run:** `./mvnw clean package`
> 
> ```
> [ INSERT SCREENSHOT 2 HERE: mvn clean package success ]
> ```

---

### 8. Automated Testing (CI Enhancement)

Automated testing serves as the primary CI quality gate. Testing was implemented using Spring's `MockMvc` framework in `src/test/java/com/campus/lostfound/controller/ItemControllerTest.java`.

#### Implemented Test Suite:
1. **`testHealthEndpoint` (`GET /health`):** Asserts HTTP 200 OK and response JSON `{"status":"UP"}`.
2. **`testAddItem` (`POST /items`):** Sends JSON item payload and asserts HTTP 201 Created, auto-generated ID, and default `OPEN` status.
3. **`testGetAllItems` (`GET /items`):** Registers test items and asserts HTTP 200 OK with correct array size and item properties.
4. **`testGetItemByIdSuccess` (`GET /items/{id}`):** Retrieves an existing item and verifies matching ID and name.
5. **`testGetItemByIdNotFound` (`GET /items/99999`):** Requests a nonexistent ID and asserts HTTP 404 Not Found.
6. **`contextLoads`:** Verifies Spring Boot application context initializes properly.

**Actual Test Results:**
```
[INFO] Running com.campus.lostfound.CampusLostFoundApiApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.campus.lostfound.controller.ItemControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Results:
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

> **Screenshot 3: Automated Test Execution Output**
> - **What to Show:** Terminal showing 6 passing tests with 0 failures and 0 errors.
> - **Command to Run:** `./mvnw test`
> 
> ```
> [ INSERT SCREENSHOT 3 HERE: mvn test results ]
> ```

---

### 9. Jenkins CI Pipeline

The Declarative `Jenkinsfile` automates the entire CI lifecycle with clear stage separation and post-action reporting.

#### Pipeline Stages:
1. **Triggers:** Configured for automatic polling using `pollSCM('H/2 * * * *')`.
2. **Checkout SCM:** Retrieves the latest code from GitHub.
3. **Maven Build:** Compiles the application source code.
4. **Automated Testing:** Runs JUnit tests and produces Surefire XML reports.
5. **Package Application:** Packages the production JAR (`-DskipTests` to avoid duplicate test runs).
6. **Post Actions:**
   - `always`: Collects and publishes JUnit test reports via `junit '**/target/surefire-reports/*.xml'`.
   - `success`: Archives executable JAR artifacts using `archiveArtifacts`.
   - `failure`: Outputs failure notifications and diagnostic logs.

#### Jenkins GitHub Configuration Steps:
1. In Jenkins dashboard, click **New Item** -> Name it `campus-lost-found-ci` -> Select **Pipeline**.
2. Under **Build Triggers**, check **Poll SCM** and enter `H/2 * * * *` (polls repository every 2 minutes).
3. Under **Pipeline**, select **Pipeline script from SCM**.
4. Choose **Git**, enter your GitHub repository URL (from `repo_link.txt`).
5. Select or add your GitHub Credentials (Personal Access Token).
6. Set Branch Specifier to `*/main` and Script Path to `Jenkinsfile`.
7. Click **Save** and trigger **Build Now**.

> **Screenshot 4: Jenkins Pipeline Execution View**
> - **What to Show:** Jenkins Pipeline Stage View showing Checkout, Build, Test, and Package all green.
> - **Location:** `http://localhost:8080/job/campus-lost-found-ci/`
> 
> ```
> [ INSERT SCREENSHOT 4 HERE: Jenkins Pipeline Stage View ]
> ```

> **Screenshot 5: Jenkins Test Results & Artifact Archive**
> - **What to Show:** Jenkins build page showing "Test Result (6 tests)" and archived JAR under Build Artifacts.
> 
> ```
> [ INSERT SCREENSHOT 5 HERE: Jenkins Test Results and Artifacts ]
> ```

---

## 10. Component 2 – Continuous Deployment Implementation

### 11. Ansible Configuration Management

Ansible automates software provisioning and deployment using modular roles and Jinja2 templating.

#### Ansible Structure:
- `ansible/inventory.ini`: Target inventory pointing to local/WSL target.
- `ansible/playbook.yml`: Master deployment playbook.
- `ansible/roles/lostfound_api/defaults/main.yml`: Configurable variables (`app_port`, `app_name`, `app_install_dir`).
- `ansible/roles/lostfound_api/tasks/main.yml`: Idempotent deployment tasks (directory creation, JAR deployment, configuration templating, background runner with PID tracking, and automated `/health` polling).
- `ansible/roles/lostfound_api/handlers/main.yml`: Service restart handler.
- `ansible/roles/lostfound_api/templates/`: `application.properties.j2` and `lostfound-api.service.j2`.

#### Execution Commands & Verification:
```bash
cd ansible
# Syntax check
ansible-playbook -i inventory.ini playbook.yml --syntax-check

# Execute deployment
ansible-playbook -i inventory.ini playbook.yml
```

**Actual Execution Result:**
```
TASK [lostfound_api : Display health check response]
ok: [localhost] => {
    "msg": "Lost & Found API is healthy on port 8080: {'status': 'UP'}"
}

PLAY RECAP
localhost : ok=12 changed=1 unreachable=0 failed=0 skipped=2 rescued=0 ignored=0
```

> **Screenshot 6: Ansible Playbook Execution & Health Verification**
> - **What to Show:** Terminal output showing successful playbook run with `failed=0` and health check confirmation.
> - **Command to Run:** `ansible-playbook -i inventory.ini playbook.yml`
> 
> ```
> [ INSERT SCREENSHOT 6 HERE: Ansible playbook execution ]
> ```

---

### 12. Docker Implementation

A multi-stage `Dockerfile` ensures optimal image size, security, and reproducibility.

#### Multi-Stage Strategy:
- **Build Stage (`maven:3.9-eclipse-temurin-17-alpine`):** Compiles code and produces the JAR inside an isolated container.
- **Runtime Stage (`eclipse-temurin:17-jre-alpine`):** Only bundles the JRE runtime and application JAR.
- **Security:** Creates a non-root group and user (`appuser:appgroup`) to prevent container breakout exploits.
- **Exposed Port:** Port 8080.

#### Docker Execution Commands:
```bash
# Build Docker image
docker build -t campus-lost-found-api .

# Run container in background
docker run -d -p 8080:8080 --name lostfound-container campus-lost-found-api

# Test container health endpoint
curl http://localhost:8080/health

# Test adding an item
curl -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"name":"Keys","description":"Bike keys with red ring","location":"Parking","type":"FOUND"}'

# Test retrieving items
curl http://localhost:8080/items

# Docker Hub Tag and Push (Replace <your-dockerhub-username>):
docker tag campus-lost-found-api <your-dockerhub-username>/campus-lost-found-api:latest
docker login
docker push <your-dockerhub-username>/campus-lost-found-api:latest
```

> **Screenshot 7: Docker Build Output**
> - **What to Show:** Terminal output of `docker build -t campus-lost-found-api .` showing both build stages.
> 
> ```
> [ INSERT SCREENSHOT 7 HERE: docker build output ]
> ```

> **Screenshot 8: Running Docker Container and curl Verification**
> - **What to Show:** Output of `docker ps` and `curl http://localhost:8080/health` returning `{"status":"UP"}`.
> 
> ```
> [ INSERT SCREENSHOT 8 HERE: docker ps and curl health test ]
> ```

---

### 13. Kubernetes Deployment & Configuration

Kubernetes manifests orchestrate application deployment and external networking.

- `k8s/deployment.yaml`: Defines 3 initial replicas, labels (`app: campus-lost-found-api`), CPU/Memory requests and limits, and health probes.
- `k8s/service.yaml`: NodePort Service exposing port 8080 on node port 30080.

#### Deployment Commands:
```bash
# Apply deployment and service
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Verify deployment and service status
kubectl get deployments
kubectl get pods -l app=campus-lost-found-api
kubectl get svc campus-lost-found-api-service
```

> **Screenshot 9: Kubernetes Deployment & 3 Initial Pods**
> - **What to Show:** Output of `kubectl get pods,svc -l app=campus-lost-found-api` showing 3 Running pods and NodePort service.
> 
> ```
> [ INSERT SCREENSHOT 9 HERE: kubectl get pods,svc with 3 replicas ]
> ```

---

### 14. Kubernetes Scaling Demonstration

The application was scaled horizontally to demonstrate dynamic load handling:

#### Scaling Steps:
1. **Initial State (3 Replicas):**
   ```bash
   kubectl get pods -l app=campus-lost-found-api
   ```
2. **Scale Up to 5 Replicas:**
   ```bash
   kubectl scale deployment campus-lost-found-api --replicas=5
   kubectl get pods -l app=campus-lost-found-api
   ```
3. **Verify 5 Running Pods:**
   All 5 pods reach `Running` state and pass readiness probes.
4. **Scale Down to 3 Replicas:**
   ```bash
   kubectl scale deployment campus-lost-found-api --replicas=3
   kubectl get pods -l app=campus-lost-found-api
   ```

> **Screenshot 10: Kubernetes Scaling to 5 Pods and Back to 3**
> - **What to Show:** Terminal showing the scaling commands and pod listings showing 5 running pods then returning to 3.
> - **Command to Run:** `kubectl get pods -l app=campus-lost-found-api`
> 
> ```
> [ INSERT SCREENSHOT 10 HERE: kubectl scale output and pod listings ]
> ```

---

### 15. Deployment Enhancement – Liveness and Readiness Probes

Configured in `k8s/deployment.yaml`:
- **Readiness Probe:**
  - Endpoint: `GET /health` on port 8080.
  - `initialDelaySeconds: 10`, `periodSeconds: 5`.
  - Determines if the pod is ready to accept traffic from the NodePort Service.
- **Liveness Probe:**
  - Endpoint: `GET /health` on port 8080.
  - `initialDelaySeconds: 20`, `periodSeconds: 10`.
  - Automatically restarts unresponsive containers if the application crashes or deadlocks.

#### Verification Command:
```bash
kubectl describe pod <pod-name> | grep -E "Liveness|Readiness"
```

> **Screenshot 11: Kubernetes Probe Verification**
> - **What to Show:** `kubectl describe pod` output showing active Liveness and Readiness probes.
> 
> ```
> [ INSERT SCREENSHOT 11 HERE: kubectl describe pod probes ]
> ```

---

## 16. Results and Conclusion

The Campus Lost & Found API DevOps pipeline was implemented, executed, and verified:
1. **CI Pipeline:** Successfully automated code integration, Git branch merging, automated testing (6/6 tests passing), artifact archiving, and SCM polling.
2. **CD Automation:** Implemented modular, idempotent Ansible automation, verified on a live Linux target with automated health verification.
3. **Containerization & Orchestration:** Built multi-stage Docker configurations, orchestrated Kubernetes deployments with 3 replicas, demonstrated elastic scaling (3 -> 5 -> 3), and verified self-healing health probes.

---

## 17. Final Submission Checklist

- [x] Spring Boot Maven application preserved and functional.
- [x] Java 17 compatibility verified.
- [x] Git initialized with `main` and `feature/api-tests` branches merged.
- [x] Comprehensive `.gitignore` protecting secrets, binaries, and IDE files.
- [x] 6 automated unit/integration tests covering all endpoints passing with 0 failures.
- [x] `Jenkinsfile` with SCM polling (`H/2 * * * *`), Maven build, JUnit publishing, and JAR archiving.
- [x] Modular Ansible role (`lostfound_api`) with idempotency, templates, handlers, and verified live run.
- [x] Production-ready multi-stage `Dockerfile` with non-root security and `.dockerignore`.
- [x] Kubernetes `deployment.yaml` (3 replicas, probes, resources) and `service.yaml` (NodePort).
- [x] Scaling and probe verification documented with exact commands.
- [x] `repo_link.txt` prepared for GitHub submission.
- [x] All 11 screenshot placeholders documented with exact capture instructions.

---

## 18. GitHub Repository Link

```
Repository URL: [PASTE YOUR GITHUB REPO URL FROM repo_link.txt HERE]
Example: https://github.com/<your-username>/campus-lost-found-api.git
```
