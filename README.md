# Payment Gateway API

A focused Java 17 / Spring Boot 4 REST API that demonstrates a payment-intent lifecycle with JWT authentication and PostgreSQL persistence.

## Implemented slice

- Issue a short-lived demo JWT using client credentials.
- Create a payment intent in `REQUIRES_CONFIRMATION` state.
- Retrieve an intent owned by the authenticated JWT subject.
- Confirm an intent into `SUCCEEDED` state; repeated confirmation is idempotent.
- Validate API requests and return structured errors.
- Create the PostgreSQL schema with Flyway and validate it with Hibernate.
- Exercise the service with unit tests and the complete HTTP flow with Testcontainers/PostgreSQL.

Amounts use the currency's minor unit: `2599` means CAD 25.99. This milestone simulates successful confirmation; it does **not** contact a card network or store card data.

## Run locally

Requirements: Java 17 and Docker with Docker Compose.

```bash
docker compose up -d
./mvnw spring-boot:run
```

The development defaults are `demo-client` / `change-me`. Override `DEMO_CLIENT_SECRET` and `JWT_SECRET` outside local development. `JWT_SECRET` must be a Base64-encoded key of at least 32 bytes.

Get a token:

```bash
curl -s http://localhost:8080/api/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"clientId":"demo-client","clientSecret":"change-me"}'
```

Use the returned `accessToken`:

```bash
curl -i http://localhost:8080/api/payment-intents \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{"amount":2599,"currency":"CAD","description":"Order 1001"}'

curl http://localhost:8080/api/payment-intents/PAYMENT_INTENT_ID \
  -H 'Authorization: Bearer YOUR_TOKEN'

curl -X POST http://localhost:8080/api/payment-intents/PAYMENT_INTENT_ID/confirm \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

## Test

Docker must be running because the integration test starts a disposable PostgreSQL 17 container.

```bash
./mvnw test
```

## Configuration

| Environment variable | Development default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/payment_gateway` |
| `DB_USERNAME` | `payment_gateway` |
| `DB_PASSWORD` | `payment_gateway` |
| `DEMO_CLIENT_ID` | `demo-client` |
| `DEMO_CLIENT_SECRET` | `change-me` |
| `JWT_SECRET` | documented development key |
| `JWT_TTL` | `PT1H` |

## Scope intentionally deferred

Kafka/outbox publishing, Redis idempotency keys, refunds, real payment-provider integration, and production identity management belong in later milestones.
