# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Warehouse management microservice for the Logisto platform (`kz.logisto`). Part of a larger "Mercurio" system — manages items, item variants, storage points, and inventory movements across warehouses and points of sale. Multi-tenant by `organizationId`.

## Build & Run

```bash
./mvnw clean install              # Build + run tests
./mvnw spring-boot:run -Dspring-boot.run.profiles=local  # Run locally
./mvnw test                       # Run all tests
./mvnw test -Dtest=ItemServiceTest # Run a single test class
./mvnw test -Dtest="ItemServiceTest#methodName" # Run a single test method
```

**Local infra:** `middleware.yml` provides PostgreSQL via Docker Compose (`docker compose -f middleware.yml up`). Local profile connects to `localhost:2345/mercurio`.

## Tech Stack

- Java 21, Spring Boot 3.5.5, Spring Data JPA, PostgreSQL
- Flyway migrations in `src/main/resources/db/migration/`
- OAuth2 resource server (Keycloak) for auth; JWT-based
- Lombok + MapStruct (annotation processors configured in `pom.xml` — Lombok must be listed before MapStruct)
- Springdoc OpenAPI (Swagger UI at `/warehouse-service/swagger-ui/index.html`)
- Micrometer/Prometheus metrics on `/actuator/prometheus`

## Architecture

### Domain Model

All entities are scoped to an `organizationId` (UUID). The core domain:

- **Item** → has many **ItemVariant** (SKU/barcode/price)
- **PointOfStorage** — warehouses or points of sale (enum `PointOfStorageType`)
- **ItemVariantPointOfStorage** — junction table tracking quantity/reserved per variant per storage location (composite key)
- **ItemVariantMovement** — records inventory movements (PURCHASE, SALE, TRANSFER, RETURN, WRITE_OFF, RESERVE) between storage points

### Layered Structure

`controller` → `service` (interface) → `service.impl` → `data.repository` (Spring Data JPA)

- **DTOs** (`data.dto.*`) — request objects, organized by domain (records)
- **Models** (`data.model.*`) — response objects
- **Mappers** (`mapper.*`) — MapStruct mappers for entity↔DTO/model conversion
- **Entities** (`data.entity.*`) — JPA entities with Lombok annotations

### Security

Per-entity security filter chains defined in `config.security.*SecurityConfig`, all extending `AbstractSecurityConfig` (configures JWT OAuth2 resource server, disables CORS/CSRF). Each entity has its own `SecurityFilterChain` bean with path-specific matchers.

Authorization is enforced at the service layer via `AccessService`, which calls the external **mc-user-service** (via `RestClient` with OAuth2 client credentials) to verify membership and warehouse management permissions.

### Inter-Service Communication

`UserServiceImpl` calls `mc-user-service` REST API for access checks using Spring's `RestClient` with `OAuth2ClientHttpRequestInterceptor`. Connection properties configured via `RestProperty` (`application.rest.mc-user-service.*`).

## Database

Schema: `mc_warehouse_service`. PostgreSQL custom enum types used for `movement_type` and `point_of_storage_type` (mapped via `PostgreSQLEnumJdbcType`).

## Spring Profiles

- `local` — local dev with localhost DB/Keycloak
- `prod` — production configuration
