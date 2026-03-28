# Event-Driven Order System

Spring Boot REST API that demonstrates an event-driven order lifecycle (order -> payment -> inventory -> notification) using Kafka. It focuses on reliability patterns (transactional outbox), idempotency, and asynchronous processing while keeping the business domain small and testable. The service uses PostgreSQL for persistence, Redis for read caching, and JWT for stateless authentication.

## Key Features

- Transactional outbox pattern: domain events are persisted to `outbox_events` and published to Kafka by a scheduled job
- Asynchronous order lifecycle across bounded contexts (order, payment, inventory, notification) using Kafka consumers and event chaining
- Idempotent payment processing via unique idempotency keys (both internal payment simulation and Stripe webhook processing)
- Redis-backed caching for read-heavy endpoints with explicit cache eviction on state transitions
- Optimistic locking (`@Version`) on core entities to protect against lost updates
- Stateless JWT auth (Spring Security filter) with role-based admin endpoints
- Integration tests using Testcontainers (PostgreSQL, Kafka, Redis) plus Awaitility for async assertions

## High-level Architecture

**Separation of concerns**

- HTTP layer: controllers for auth, products, orders, Stripe webhook
- Domain services: business logic per module (order/payment/inventory/notification)
- Persistence: PostgreSQL + Flyway migrations
- Messaging: Kafka topics as the integration boundary
- Caching: Redis via Spring Cache

**Event flow (happy path)**

1. `POST /api/orders` persists an order and writes `OrderCreatedEvent` to the outbox
2. `PATCH /api/orders/{id}/pay` writes `OrderPaymentRequestedEvent` to the outbox
3. Outbox publisher sends events to Kafka (topic determined from event class name)
4. Payment consumer processes request and publishes `PaymentSucceededEvent` or `PaymentFailedEvent`
5. Order consumer updates order state and publishes follow-up events (`OrderPaidEvent`, `OrderCancelledEvent`)
6. Inventory consumer reserves stock on `OrderPaidEvent` and publishes `InventoryReservedEvent`
7. Order consumer completes the order on `InventoryReservedEvent` and publishes `OrderCompletedEvent`
8. Notification consumer reacts to `OrderCompletedEvent` (currently logs)

**Stripe checkout flow (async)**

- `POST /api/orders/{id}/payments/stripe` records a `stripe_checkout_requests` row and emits `OrderStripeCheckoutRequestedEvent`
- A Kafka consumer attempts to create a Stripe Checkout session and updates the request as `READY` with `checkoutUrl` or `FAILED`
- `GET /api/orders/{id}/payments/stripe` returns the current request status for polling
- `POST /api/stripe/webhook` deduplicates webhook deliveries and emits payment result events

## Notable Design Decisions

- Outbox publishing is decoupled from request transactions: events are written to Postgres first, then a scheduled job publishes and marks `published_at`
- Kafka topic routing is derived from event type naming (`.order.`, `.payment.`, `.inventory.`) to keep producers simple
- Webhook deduplication uses a persisted `stripe_webhook_events` table and a unique payment idempotency key per Stripe event id
- Internal payment processing is intentionally deterministic (amount vs expected amount) to keep the async event choreography testable
- Caching is scoped to specific reads (`orders`, `productById`) with TTLs and explicit eviction on state changes

## Project Structure

```text
src/
  main/
    java/com/example/ordersystem/
      config/        # Security, Redis cache manager, OpenAPI
      inventory/     # Product catalog + stock reservation
      notification/  # Notification listener/service (log-based)
      order/         # Order API, lifecycle, Stripe checkout request tracking
      payment/       # Payment processing + Stripe webhook handling
      shared/        # Outbox, domain events, security, exceptions, base entities
    resources/
      application.yml               # runtime config (DB, Kafka, Redis)
      db/migration/                 # Flyway migrations
  test/
    java/com/example/ordersystem/   # Integration tests (Testcontainers + Awaitility)
    resources/
      application-test.properties   # test-only config
```

## Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.x |
| API | Spring Web (REST), Spring Validation |
| Database | PostgreSQL, Flyway |
| ORM | Spring Data JPA (Hibernate) |
| Messaging | Apache Kafka (Spring Kafka) |
| Caching | Redis (Spring Cache) |
| Auth | Spring Security + JWT (JJWT) |
| API docs | springdoc-openapi (Swagger UI) |
| Mapping | MapStruct |
| Build / format | Maven Wrapper, Spotless (google-java-format) |
| Testing | JUnit 5, Testcontainers (Postgres/Kafka/Redis), Awaitility |

## Running the Project

### Requirements

- Java 21
- Docker (for PostgreSQL/Redis/Kafka via Compose)

### Option 1: Docker (recommended)

Start infrastructure:

```bash
docker compose up -d
```

Create a `.env` file (see Configuration section) and run the app:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
.\mvnw.cmd spring-boot:run
```

Useful local URLs:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Kafka UI: `http://localhost:8081`

### Option 2: Local setup (without Docker)

Provide running instances of PostgreSQL, Redis, and Kafka, then set the required environment variables and start:

```bash
./mvnw spring-boot:run
```

## Configuration (Environment variables)

The application loads environment variables from `.env` on startup (via `dotenv-java`) and fails fast when required values are missing.

Required:

- `SECRET_KEY` - JWT signing key (HMAC)
- `DB_NAME` - PostgreSQL database name
- `DB_USER` - PostgreSQL username
- `DB_PASSWORD` - PostgreSQL password
- `DB_PORT` - local port mapped to Postgres (used by `application.yml`)

Optional (Stripe):

- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`
- `STRIPE_CURRENCY` (default: `pln`)
- `STRIPE_SUCCESS_URL` (default points to Swagger UI)
- `STRIPE_CANCEL_URL` (default points to Swagger UI)

## Endpoints

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/auth/profile`

### Orders

- `GET /api/orders`
- `GET /api/orders/admin`
- `POST /api/orders`
- `GET /api/orders/{id}`
- `PATCH /api/orders/{id}/pay`
- `POST /api/orders/{id}/payments/stripe`
- `GET /api/orders/{id}/payments/stripe`

### Products

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`

### Stripe

- `POST /api/stripe/webhook`

### Ops

- `GET /health`

## Testing Strategy

- Integration tests: exercise HTTP endpoints and async Kafka-driven workflows using Testcontainers for PostgreSQL, Kafka, and Redis
- Async assertions: Awaitility is used to wait for state transitions caused by background consumers/outbox publishing

Run tests:

```bash
./mvnw test
```

## What I learned

- Designing an event-driven workflow with clear domain boundaries and Kafka consumer groups
- Implementing a transactional outbox publisher to avoid dual-write problems
- Making async processing testable with Testcontainers + Awaitility
- Applying idempotency patterns for payments and webhook retries
- Using Redis caching safely with explicit eviction on writes
