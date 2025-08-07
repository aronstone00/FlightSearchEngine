# Flight Search POC – Docker-ready

A minimal proof-of-concept that ingests flight information into **PostgreSQL** and indexes it into **Elasticsearch** so that clients can perform fast, paginated & sorted searches – all shipped as a **single Spring-Boot JAR** and a **one-command `docker-compose` stack**.

```
Clients  ──▶  Flight-Search-POC (Java) ──▶ PostgreSQL (write / read)
                            └────▶ Elasticsearch  (index / search) ──▶ Clients
```

## 1  Run with Docker 🐳

```bash
# clone repository then:
cd flight-search-poc

docker-compose up --build   # first run compiles the JAR, then starts PG + ES + app
```

* `localhost:8080/swagger-ui/index.html` – interactive API docs
* `localhost:5432` – Postgres (`postgres/secret`, DB `flights_db`)
* `localhost:9200` – Elasticsearch (`elastic/changeme`)

Stop & remove:

```bash
docker-compose down          # keep data volumes
# or
docker-compose down -v       # also wipe Postgres & ES data
```

### Environment mapping (handled by compose)

| Spring property                     | compose env-var              | example value          |
|------------------------------------|------------------------------|------------------------|
| `PG_URL` / `PG_USERNAME` / `PG_PASSWORD` | set for Postgres service   | `jdbc:postgresql://postgres:5432/flights_db` |
| `ELASTICSEARCH_HOST` / `ELASTICSEARCH_PORT` | internal DNS + port       | `elasticsearch` / `9200` |
| `ELASTICSEARCH_USERNAME` / `ELASTICSEARCH_PASSWORD` | `elastic` / `changeme` | – |

The app waits for both containers to become **healthy**, then starts.

## 2  Local JVM (no Docker)

You can still run directly:

```bash
mvn clean package
java -jar target/flight-search-poc-1.0.0.jar \
  --PG_URL=jdbc:postgresql://localhost:5432/flights_db \
  --PG_USERNAME=postgres --PG_PASSWORD=secret \
  --ELASTICSEARCH_HOST=localhost --ELASTICSEARCH_PORT=9200 \
  --ELASTICSEARCH_USERNAME=elastic --ELASTICSEARCH_PASSWORD=changeme
```

Create the DB table & ES index once:

```bash
psql $PG_URL -U $PG_USERNAME -f migrations/V1__create_flights_table.sql
curl -u $ELASTICSEARCH_USERNAME:$ELASTICSEARCH_PASSWORD \
     -H 'Content-Type: application/json' \
     -X PUT "http://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/flights" \
     -d @es-mapping.json
```

## 3  Sample workflow

```bash
# ingest three flights
airports=(1500 500 1000)
for i in ${!airports[@]}; do
  curl -X POST http://localhost:8080/api/v1/flights -H 'Content-Type: application/json' -d "{\
    \"flightId\":\"T-00$((i+1))\",\
    \"airline\":\"Demo\",\
    \"departureCity\":\"BLR\",\
    \"arrivalCity\":\"MOUM\",\
    \"departureTime\":\"2025-08-07T0$((i+3)):00:00Z\",\
    \"basePrice\":${airports[$i]} }"
done

# verify search ordered by price asc
curl 'http://localhost:8080/api/v1/search?departureCity=BLR&arrivalCity=MOUM&departureDate=2025-08-07&sort=basePrice,asc' | jq '.content | map({flightId,basePrice})'
```

Expected response:

```json
[
  { "flightId": "T-002", "basePrice": 500.0 },
  { "flightId": "T-003", "basePrice": 1000.0 },
  { "flightId": "T-001", "basePrice": 1500.0 }
]
```

## 4  Tech highlights

* **Idempotency** – `flightId` is unique in Postgres and reused as ES doc-id (`upsert`).
* **Retry** – Ingestion retries ES indexing 3× with exponential back-off (covers initial container warm-up).
* **Observability** – `/healthz` liveness plus Spring Actuator `/actuator/health`, structured logs.
* **OpenAPI** – Swagger UI included via springdoc-openapi.
* **Single-jar** dev mode, yet still split logically into *ingestion* and *search* packages.

Enjoy exploring ✈️
