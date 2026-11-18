# Campus Lost & Found API - DevOps Project

A Spring Boot RESTful API designed to manage lost and found items on a university campus, fully configured with an end-to-end DevOps pipeline including Git branching, Maven builds, automated unit/integration testing, Jenkins CI, Ansible configuration management, Docker containerization, and Kubernetes orchestration.

---

## 1. Project Overview

- **Application Name:** `campus-lost-found-api`
- **Package:** `com.campus.lostfound`
- **Framework:** Spring Boot 4.1.2 / Java 17
- **Build Tool:** Apache Maven
- **Storage:** In-memory `ConcurrentHashMap` with thread-safe atomic counter

### API Endpoints

| Method | Endpoint | Description | Expected Status |
|--------|----------|-------------|-----------------|
| `GET`  | `/health` | Application health status | `200 OK` (`{"status":"UP"}`) |
| `POST` | `/items`  | Register a lost or found item | `201 Created` |
| `GET`  | `/items`  | Retrieve all registered items | `200 OK` |
| `GET`  | `/items/{id}` | Retrieve item by unique ID | `200 OK` / `404 Not Found` |

---

## 2. Project Structure

```
campus-lost-found-api/
│
├── src/
│   ├── main/java/com/campus/lostfound/
│   │   ├── CampusLostFoundApiApplication.java
│   │   ├── controller/ItemController.java
│   │   ├── model/ (Item.java, ItemStatus.java, ItemType.java)
│   │   └── service/ItemService.java
│   └── test/java/com/campus/lostfound/
│       ├── CampusLostFoundApiApplicationTests.java
│       └── controller/ItemControllerTest.java
│
├── ansible/
│   ├── inventory.ini
│   ├── playbook.yml
│   └── roles/lostfound_api/
│       ├── defaults/main.yml
│       ├── handlers/main.yml
│       ├── tasks/main.yml
│       └── templates/ (application.properties.j2, lostfound-api.service.j2)
│
├── k8s/
│   ├── deployment.yaml
│   └── service.yaml
│
├── Dockerfile
├── .dockerignore
├── Jenkinsfile
├── .gitignore
├── pom.xml
├── repo_link.txt
├── README.md
└── REPORT_GUIDE.md
```

---

## 3. Quick Start & Execution Guide

### A. Local Maven Build and Test

```bash
# Run unit and integration tests
./mvnw clean test

# Package executable JAR
./mvnw clean package

# Run Spring Boot application
java -jar target/campus-lost-found-api-0.0.1-SNAPSHOT.jar
```

### B. Docker Build and Run

```bash
# Build multi-stage Docker image
docker build -t campus-lost-found-api .

# Run container exposing port 8080
docker run -d -p 8080:8080 --name lostfound-app campus-lost-found-api

# Test container health
curl http://localhost:8080/health
```

### C. Ansible Deployment

```bash
cd ansible
# Verify syntax
ansible-playbook -i inventory.ini playbook.yml --syntax-check

# Deploy application
ansible-playbook -i inventory.ini playbook.yml
```

### D. Kubernetes Deployment & Scaling

```bash
# Deploy to Minikube / Kubernetes
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Verify 3 replicas
kubectl get pods -l app=campus-lost-found-api

# Scale to 5 replicas
kubectl scale deployment campus-lost-found-api --replicas=5
kubectl get pods -l app=campus-lost-found-api

# Scale back to 3 replicas
kubectl scale deployment campus-lost-found-api --replicas=3
kubectl get pods -l app=campus-lost-found-api
```
DevOps CI verification update.