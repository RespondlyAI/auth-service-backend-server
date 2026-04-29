# RespondlyAI Auth Service

A **Spring Boot + Groovy** microservice that handles authentication and JWT-based authorization for the RespondlyAI platform.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Data Model](#data-model)
- [JWT Token](#jwt-token)
- [Error Handling](#error-handling)
- [Configuration](#configuration)
- [Local Development Setup](#local-development-setup)
- [Database Migrations](#database-migrations)
- [Running Tests](#running-tests)

---

## Overview

The Auth Service is responsible for:

- **User registration (signup)** — restricted to users with the `OWNER` role; email must be a valid `@gmail.com` address.
- **User login** — authenticates via email/password and returns a signed JWT access token.
- **Stateless JWT authentication** — every protected request must supply a `Bearer <token>` in the `Authorization` header.
- **Role-based access control** — supports three roles: `OWNER`, `ADMIN`, and `EMPLOYEE`.
- **Interactive API docs** — Swagger UI is available at `http://localhost:8080/swagger-ui/`.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Groovy 4 (Apache Groovy) |
| Framework | Spring Boot 4.0.3 |
| Security | Spring Security + JJWT 0.12.5 |
| Persistence | Spring Data JPA + PostgreSQL 17 |
| Schema migrations | Flyway |
| Validation | Jakarta Bean Validation |
| API docs | SpringDoc OpenAPI (Swagger UI) |
| Build tool | Gradle (Groovy DSL) |
| Java version | Java 25 |
| Containerisation | Docker Compose (local dev DB) |

---

## Project Structure

```
src/
└── main/
│   ├── groovy/in/respondlyai/auth/
│   │   ├── AuthApplication.groovy          # Spring Boot entry point
│   │   ├── config/
│   │   │   ├── SecurityConfig.groovy       # Spring Security configuration (JWT filter, BCrypt, CSRF off, stateless)
│   │   │   └── SwaggerConfig.groovy        # OpenAPI / Swagger UI configuration
│   │   ├── controller/
│   │   │   └── AuthController.groovy       # REST endpoints: POST /api/auth/signup, POST /api/auth/login
│   │   ├── dto/
│   │   │   ├── LoginRequest.groovy         # Login payload (email, password)
│   │   │   ├── SignupRequest.groovy        # Signup payload (name, email, password, role)
│   │   │   └── AuthResponse.groovy         # Response DTO (token, userId, email, role)
│   │   ├── entity/
│   │   │   ├── User.groovy                 # JPA entity mapped to `users` table
│   │   │   └── Role.groovy                 # Enum: OWNER | ADMIN | EMPLOYEE
│   │   ├── exception/
│   │   │   ├── ApiException.groovy         # Custom runtime exception with factory helpers
│   │   │   ├── ApiErrorResponse.groovy     # Structured JSON error body
│   │   │   ├── ErrorType.groovy            # Enum of error categories
│   │   │   └── GlobalExceptionHandler.groovy # @ControllerAdvice — centralised error mapping
│   │   ├── repository/
│   │   │   └── UserRepository.groovy       # JpaRepository<User, UUID> with custom finders
│   │   ├── security/jwt/
│   │   │   ├── JwtService.groovy           # Token generation, validation, claim extraction
│   │   │   └── JwtAuthenticationFilter.groovy # OncePerRequestFilter — validates Bearer tokens
│   │   └── service/
│   │       ├── AuthService.groovy          # Core signup / login business logic
│   │       └── AppUserDetailsService.groovy # UserDetailsService bridge for Spring Security
│   └── resources/
│       ├── application.properties          # Base configuration (port, JPA, Flyway)
│       ├── application-local.example.properties # Template for local dev secrets
│       └── db/migration/
│           ├── V1__create_users_table.sql  # Creates the `users` table
│           └── V2__rename_organization_column.sql # Renames `organization` → `organization_id`
└── test/
    └── groovy/in/respondlyai/auth/
        └── AuthApplicationTests.groovy     # Spring context load test
```

---

## API Endpoints

Base URL: `http://localhost:8080`

### `POST /api/auth/signup`

Registers a new user. Only `OWNER` role is permitted.

**Request body**

```json
{
  "name": "Jane Doe",
  "email": "jane@gmail.com",
  "password": "secret123",
  "role": "OWNER"
}
```

**Responses**

| Status | Meaning |
|---|---|
| `201 Created` | User created; JWT returned in body and `Authorization` header |
| `400 Bad Request` | Validation failure (e.g. missing name, weak password) |
| `403 Forbidden` | Role is not `OWNER` |
| `409 Conflict` | Email already registered |
| `500 Internal Server Error` | Unexpected server error |

---

### `POST /api/auth/login`

Authenticates an existing user.

**Request body**

```json
{
  "email": "jane@gmail.com",
  "password": "secret123"
}
```

**Responses**

| Status | Meaning |
|---|---|
| `200 OK` | Login successful; JWT returned in body and `Authorization` header |
| `400 Bad Request` | Missing/invalid credentials |
| `401 Unauthorized` | Wrong email or password |

---

### Successful response body (both endpoints)

```json
{
  "token": "<JWT>",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "jane@gmail.com",
  "role": "OWNER"
}
```

The JWT is also returned as a `Bearer` token in the `Authorization` response header.

---

## Data Model

### `users` table

| Column | Type | Notes |
|---|---|---|
| `uuid` | UUID | Primary key, auto-generated |
| `user_id` | VARCHAR | Application-level unique identifier |
| `name` | VARCHAR | Full name, required |
| `email` | VARCHAR | Unique, required |
| `password` | VARCHAR | BCrypt-hashed |
| `is_verified` | BOOLEAN | Default `false` |
| `role` | VARCHAR | `OWNER` / `ADMIN` / `MEMBER` |
| `organization_id` | VARCHAR | Nullable |
| `created_at` | TIMESTAMP | Set on insert |
| `updated_at` | TIMESTAMP | Updated on every save |

---

## JWT Token

Tokens are signed with **HMAC-SHA256** using a secret key that must be at least 32 bytes (256 bits).

**Custom claims embedded in the token:**

| Claim | Value |
|---|---|
| `sub` | User's email address |
| `userId` | Application user ID |
| `role` | User role string |
| `organizationId` | Organization ID (omitted if null) |

Default expiry: **24 hours** (`86400000` ms).  
Refresh-token expiry: **7 days** (`604800000` ms) — configurable, not yet implemented as a separate endpoint.

---

## Error Handling

All errors follow a consistent JSON structure:

```json
{
  "success": false,
  "message": "Human-readable error description",
  "type": "VALIDATION_ERROR",
  "timestamp": "2026-03-01T10:00:00.000Z"
}
```

Error types: `VALIDATION_ERROR`, `AUTH_ERROR`, `FORBIDDEN`, `CONFLICT`, `BAD_REQUEST`, `INTERNAL_SERVER_ERROR`.

---

## Configuration

### `application.properties` (base — committed)

```properties
spring.application.name=auth-service
server.port=8080
spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### `application-local.properties` (not committed — create from example)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=postgres
spring.datasource.password=password

application.security.jwt.secret-key=<at-least-32-char-secret>
application.security.jwt.expiration=86400000
application.security.jwt.refresh-token.expiration=604800000
```

---

## Local Development Setup

### Prerequisites

- Java 25+
- Docker & Docker Compose
- Gradle (or use the included `./gradlew` wrapper)

### Steps

1. **Clone the repository**

   ```bash
   git clone https://github.com/RespondlyAI/auth-service-backend-server.git
   cd auth-service-backend-server
   ```

2. **Start the local PostgreSQL database**

   ```bash
   docker compose up -d
   ```

   This starts a PostgreSQL 17 container on port `5432` with database `auth_db`.

3. **Create your local properties file**

   ```bash
   cp src/main/resources/application-local.example.properties \
      src/main/resources/application-local.properties
   ```

   Edit `application-local.properties` and fill in your database credentials and a strong JWT secret (at least 32 characters).

   Generate a secure secret:
   ```bash
   openssl rand -base64 32
   ```

4. **Run the application**

   ```bash
   ./gradlew bootRun
   ```

   The server starts on `http://localhost:8080`.

5. **Explore the API**

   Open `http://localhost:8080/swagger-ui/` in your browser for interactive API documentation.

---

## Database Migrations

Flyway runs automatically on startup. Migration scripts live in `src/main/resources/db/migration/`:

| File | Description |
|---|---|
| `V1__create_users_table.sql` | Creates the `users` table with all columns and constraints |
| `V2__rename_organization_column.sql` | Renames column `organization` → `organization_id` |

---

## Running Tests

```bash
./gradlew test
```

The test suite requires no running database — the context load test excludes `DataSourceAutoConfiguration`.
.
