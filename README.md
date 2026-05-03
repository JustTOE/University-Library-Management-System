# ULMS — University Library Management System

Software Development Methods coursework, UPB Bucharest. Spring Boot 4 backend (`src/ulms/`) plus a SvelteKit frontend skeleton (`src/web/ulms-web/`). Postgres via docker-compose.

## Running locally

```sh
docker compose up -d postgres_database
cd src/ulms
./mvnw spring-boot:run
```

The first boot runs Flyway migrations against the `ulms` database.

## Required environment variables (backend)

Set these before running outside the docker-compose defaults:

| Variable | Default | Notes |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5431/ulms` | match your Postgres |
| `SPRING_DATASOURCE_USERNAME` | `db_user` | |
| `SPRING_DATASOURCE_PASSWORD` | `db_password` | |
| `JWT_SECRET` | _(none — required, ≥ 32 bytes)_ | HS256 signing key; the app refuses to start in non-`test` profiles without it |
| `JWT_LIFETIME_MINUTES` | `60` | Access token TTL |
| `ULMS_FRONTEND_ORIGIN` | `http://localhost:5173` | CORS allow-list (single origin) |

## Bootstrap admin

Migration `V13__seed_admin.sql` inserts a default admin you can use to get going:

- email: `admin@ulms.local`
- password: `admin-change-me-now`

This password is documented (it is **not** a secret) — rotate it on first login of any environment that's reachable from the network. There is no force-reset flow yet, so for now: log in, then `PUT /api/users/{id}` (admin-only) to update the password through the regular create-user code path, or run a one-shot UPDATE on the `users` table with a freshly BCrypt-hashed value.

## Auth surface

- `POST /api/auth/register` — STUDENT-only public registration. Returns the created `UserResponse`.
- `POST /api/auth/login` — `{email, password}` → `{token, expiresAt, role, userId, name}`. After 5 consecutive failed attempts the account is locked for 15 minutes (HTTP 423 + `Retry-After`).
- `GET /api/auth/me` — current `UserResponse` for the bearer in `Authorization`.
- All other `/api/**` endpoints require a valid JWT; per-method `@PreAuthorize` enforces RBAC.

## Tests

```sh
cd src/ulms
./mvnw test
```

H2 with PostgreSQL mode is the test datasource; the seed admin migration has an H2-flavoured mirror under `src/test/resources/db/migration-h2`.
