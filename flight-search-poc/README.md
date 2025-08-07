# Flight Search POC

Minimal two-microservice proof-of-concept that ingests flight information into PostgreSQL and indexes it in Elasticsearch so that clients can perform fast, paginated & sorted searches.

```
Clients → Ingestion Service (PostgreSQL)
          ↘ POST to Search Service → Elasticsearch
                                  ↘ Clients
```

## Modules

| Module | Description | Port |
|--------|-------------|------|
| **ingestion-service** | Validates & persists flights to Postgres and forwards the payload to Search Service. | **8081** |
| **search-service**    | Idempotently upserts documents into Elasticsearch and exposes a `/search` API. | **8082** |

## Quick-start

1. Clone & open the project.
2. Copy `.env.example` → `.env` and adjust credentials.
3. Build everything once (downloads dependencies):

   ```bash
   mvn -pl ingestion-service,search-service -am clean package
   ```

4. Apply DB & ES artifacts (**one-off**):

   ```bash
   psql $PG_URL -U $PG_USERNAME -f migrations/V1__create_flights_table.sql

   curl -X PUT "http://${ES_HOST}:${ES_PORT}/flights" \
        -u ${ES_USERNAME}:${ES_PASSWORD} \
        -H 'Content-Type: application/json' \
        -d @es-mapping.json
   ```

5. Run the services (in separate shells / terminals):

   ```bash
   cd ingestion-service && mvn spring-boot:run
   cd ../search-service && mvn spring-boot:run
   ```

6. Smoke test:

   ```bash
   curl -X POST http://localhost:8081/api/v1/flights \
     -H 'Content-Type: application/json' \
     -d '{"flightId":"AI-101","airline":"Air India","departureCity":"DEL","arrivalCity":"BOM","departureTime":"2025-08-10T06:30:00Z","basePrice":5499.00}'

   curl "http://localhost:8082/api/v1/search?departureCity=DEL&arrivalCity=BOM&departureDate=2025-08-10"
   ```

## Configuration Reference

All runtime config lives in `application.yml` files and can be overridden via environment variables. A convenient `.env.example` template is provided.

Ingestion Service (`src/main/resources/application.yml`):

```
spring:
  datasource:
    url: ${PG_URL}
    username: ${PG_USERNAME}
    password: ${PG_PASSWORD}
server:
  port: 8081
search.service.url: ${SEARCH_SERVICE_URL:http://localhost:8082}
```

Search Service (`src/main/resources/application.yml`):

```
elasticsearch:
  host: ${ES_HOST}
  port: ${ES_PORT}
  username: ${ES_USERNAME}
  password: ${ES_PASSWORD}
server:
  port: 8082
```

## API Reference

### Ingestion Service

| Verb | Path | Description |
|------|------|-------------|
| `POST` | `/api/v1/flights` | Persist & index a flight. **201** Created / **400** Validation / **409** Duplicate |
| `GET`  | `/api/v1/flights/{flightId}` | Retrieve a flight. **200** / **404** |

### Search Service

| Verb | Path | Description |
|------|------|-------------|
| `POST` | `/api/v1/index/flight` | Idempotent upsert (called by Ingestion Service). |
| `GET`  | `/api/v1/search` | `?departureCity=&arrivalCity=&departureDate=&page=&size=&sort=` |

Example search:

```
GET /api/v1/search?departureCity=DEL&arrivalCity=BOM&departureDate=2025-08-10&page=0&size=5&sort=basePrice,asc
```

## Acceptance Criteria

1. Given 3 flights on same route & date with prices **1000, 500, 1500** ⇒ `/search` returns `[500,1000,1500]`.
2. A new flight becomes searchable immediately after POST (POC synchronous indexing).

---

### Notes & Call-outs

* **Idempotency**: Duplicate `flightId` rejected at write-time; Search Service uses the same `flightId` as ES document id (upsert).
* **Retries**: Ingestion Service retries the index call 3× with exponential back-off; failures are logged but DB transaction is not rolled back (non-blocking write-behind semantics).
* **Extensibility**: Swapping out direct REST with Kafka is trivial – publish an event after commit instead of `RestTemplate`.
* **Observability**: Spring Actuator exposes `/actuator/health`, logs include request timing via simple `StopWatch`.
* **Version Compatibility**: ES 7.17 client is used because it’s compatible with both 7.x & most 8.x clusters in *compat* mode.

# Build the JAR
mvn clean package

# Run the unified application (will start on port 8080)
java -jar target/flight-search-poc-1.0.0.jar

# Or run with Maven
mvn spring-boot:run