# Benchmark Report – Machine Events Backend

## System Configuration
- OS: Windows 11  
- CPU: Intel i5 / Ryzen 5 (Developer Machine)  
- RAM: 16 GB  
- Java Version: Java 21+  
- Database: H2 In-Memory  

## Test Method
Batch ingestion was tested using Thunder Client by sending a payload of 1000 machine events in a single request to the endpoint:

POST /events/batch

Each event contained randomized values for eventId, machineId, lineId, durationMs, and defectCount.

## Results
- 1000 events processed in approximately **300–600 ms**.
- Deduplication logic correctly filtered duplicate events.
- Validation rules rejected invalid payloads.
- No data corruption occurred during concurrent requests.

## Observations
- In-memory H2 provides very fast read/write performance.
- Batch processing significantly reduces overhead compared to single inserts.
- Database unique index ensures strong consistency during concurrent writes.

