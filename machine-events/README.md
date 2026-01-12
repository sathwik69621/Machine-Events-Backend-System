# Machine Events Backend System

## 📌 Overview
This project is a backend service that ingests machine events in batches, validates and deduplicates them, stores them safely, and provides analytical statistics through REST APIs.

The system is designed to be thread-safe, scalable, and easy to extend.

---


## Tech Stack
- Java 21
- Spring Boot
- Spring Data JPA
- H2 In-memory Database
- visual studio(vs code)
- Maven
- JUnit 5

---

##  Architecture
Controller → Service → Repository → Database

- **Controller**: Handles REST APIs.
- **Service**: Business logic, validation, deduplication, concurrency handling.
- **Repository**: Database access using JPA.
- **Database**: H2 in-memory database.

---

## 🔄 Deduplication & Update Rules
- Same eventId + identical payload → ignored (deduplicated).
- Same eventId + different payload + newer receivedTime → updated.
- Same eventId + different payload + older receivedTime → ignored.

---

## 🔐 Thread Safety
- Database unique constraint on eventId.
- Transactional service layer.
- Concurrent inserts handled safely.

---

## Data Model
EventEntity:
- eventId
- eventTime
- receivedTime
- machineId
- durationMs
- defectCount
- factoryId
- lineId

---

## Performance Strategy
- Batch processing
- Indexed columns
- In-memory DB for fast operations

---

## Edge Cases
- Future events rejected
- Invalid duration rejected
- defectCount = -1 ignored in calculations
---

## ⚙️ Validation Rules
- durationMs must be greater than 0.
- eventTime cannot be in the future.
- defectCount = -1 is ignored in statistics.

---
## for testing  the test cases that are provided in EventServiceTest.java

bash:mvn test

---

## How to Run
bash :mvn spring-boot:run

---

## further improvements
1. Persistent Database (Very Important)
🔹 Current
    *Using H2 in-memory database
    *Data is lost when application restarts
🔹 Improvement
    *Use PostgreSQL / MySQL
    *Enable migrations using Flyway or Liquibase

2. Observability (Logs, Metrics)
🔹 Current
    *Basic logs only
🔹 Improvement
    *Add structured logging
    *Metrics using Prometheus + Grafana
    *Health checks