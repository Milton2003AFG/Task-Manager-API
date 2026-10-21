# Task Manager API

REST API for managing projects and tasks, built with Spring Boot.
Each user can only access their own data.

## Features

- User registration and login with JWT authentication
- Projects and tasks CRUD, scoped to the authenticated user
- Task filtering by status and pagination
- Per-project task statistics (count by status)
- Consistent error responses (RFC 9457 `ProblemDetail`)
- Database schema versioned with Flyway
- Integration tests against a real PostgreSQL (Testcontainers)

## Tech stack

- Java 21, Spring Boot 4.1
- Spring Web MVC, Spring Data JPA (Hibernate), Spring Security (OAuth2 Resource Server)
- PostgreSQL 18, Flyway
- JUnit 5, MockMvc, Testcontainers
- Maven

## Getting started

### Prerequisites

- JDK 21
- Docker or Podman (with the Docker-compatible socket enabled)

### Configuration

The app reads the JWT signing secret from an environment variable.
HS512 requires at least 64 bytes:

```bash
export JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
```

Optional: `JWT_ISSUER` (default `task-manager-api`).

### Run

```bash
./mvnw spring-boot:run
```

Spring Boot starts the PostgreSQL container from `compose.yaml`
automatically and Flyway applies the migrations.

> The database credentials in `compose.yaml` are for local development only.

### Run the tests

```bash
./mvnw test
```

Tests start their own PostgreSQL container with Testcontainers.

## API overview

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/register` | Create an account | No |
| POST | `/auth/login` | Get an access token | No |
| GET | `/projects` | List my projects | Yes |
| POST | `/projects` | Create a project | Yes |
| GET | `/projects/{id}` | Get a project | Yes |
| PUT | `/projects/{id}` | Update a project | Yes |
| DELETE | `/projects/{id}` | Delete a project | Yes |
| POST | `/projects/{projectId}/tasks` | Create a task | Yes |
| GET | `/projects/{projectId}/tasks?status=&page=&size=` | List tasks (filter + pagination) | Yes |
| GET | `/projects/{projectId}/tasks/{taskId}` | Get a task | Yes |
| PATCH | `/projects/{projectId}/tasks/{taskId}` | Partial update | Yes |
| DELETE | `/projects/{projectId}/tasks/{taskId}` | Delete a task | Yes |
| GET | `/projects/{projectId}/tasks/stats` | Task counts by status | Yes |


Authenticated requests send `Authorization: Bearer <token>`.

### Example

```bash
curl -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@example.com","password":"<12+ chars, upper, lower, digit, symbol>"}'

curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@example.com","password":"..."}'
```

## Design decisions

- **Ownership checks live in the queries.** Tasks are always fetched by
  task, project and owner id in a single query, so a user cannot reach
  another user's data by guessing ids.
- **404 instead of 403** for resources that belong to someone else, so the
  API does not reveal that they exist.
- **Same 401 for unknown email and wrong password** to avoid user enumeration.
- **PATCH, not PUT, for task updates:** omitted fields are left unchanged.
  An empty `description` clears it.
- **DTOs everywhere.** Entities are never returned, so `passwordHash` and
  relations are never exposed.
- **Stateless JWT (HS512)** issued by the API itself and validated by
  Spring Security's resource server, including expiration and issuer.
- **Flyway migrations** own the schema; Hibernate only validates it
  (`ddl-auto=validate`).

## Testing

Integration tests cover registration and login, JWT validation (tampered,
expired, wrong issuer, wrong secret), user isolation, partial updates,
statistics, filtering and pagination. Each security-related test was
verified to fail when the protection it covers is removed.

## Project structure

```
src/main/java/.../
├── auth/       registration and login
├── user/       User entity and repository
├── project/    projects
├── task/       tasks, filtering, statistics
├── security/   security configuration and JWT service
└── exception/  global error handling
```

## Possible improvements

- Refresh tokens
- OpenAPI documentation
- Role-based authorization
- Deployment to a cloud provider

## Run with Docker

Runs the API and PostgreSQL together, without installing a JDK.

### 1. Create the `.env` file

The API signs tokens with a secret read from `JWT_SECRET`
(HS512 needs at least 64 bytes). Generate one:

```bash
echo "JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')" > .env
```

`.env` is git-ignored. See `.env.example` for the expected format.

### 2. Start

Stop any local run from your IDE first, since both use the same
database volume.

```bash
podman compose -f compose.full.yaml up --build
```

With Docker, use `docker compose` instead of `podman compose`.

### 3. Try it

In another terminal:

```bash
curl -i -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"docker@test.com","password":"Abcdefghij12#"}'
```

You should get a `201 Created` response.

### Stop

```bash
podman compose -f compose.full.yaml down      # keeps the data
podman compose -f compose.full.yaml down -v   # also deletes the data
```

> The credentials in the Compose files (`secret`, `taskuser`) are for
> local development only. In a real deployment, provide them through
> environment variables or a secrets manager.

