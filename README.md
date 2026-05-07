# ULMS — University Library Management System

Software Development Methods coursework, UPB Bucharest. Spring Boot 4 backend (`src/ulms/`) plus a Next.js 16 + React 19 + shadcn/ui frontend (`src/web/ulms-web/`). Postgres + MailHog via docker-compose.

## Running locally

```sh
# 1. Bring up Postgres (and MailHog, optional but keeps the actuator health check green)
docker compose up -d postgres_database mailhog

# 2. Backend
cd src/ulms
JWT_SECRET=dev-only-jwt-secret-for-local-32-byte-min ./mvnw spring-boot:run

# 3. Frontend (separate terminal, with backend on :8080)
cd src/web/ulms-web
cp .env.example .env.local   # only the first time
npm install                  # only the first time
npm run dev
```

The first backend boot runs Flyway migrations against the `ulms` database. The frontend dev server listens on `http://localhost:3000`. Sign in with the seeded admin credentials below.

## Required environment variables (backend)

Set these before running outside the docker-compose defaults:

| Variable | Default | Notes |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5431/ulms` | match your Postgres |
| `SPRING_DATASOURCE_USERNAME` | `db_user` | |
| `SPRING_DATASOURCE_PASSWORD` | `db_password` | |
| `JWT_SECRET` | _(none — required, ≥ 32 bytes)_ | HS256 signing key; the app refuses to start in non-`test` profiles without it |
| `JWT_LIFETIME_MINUTES` | `60` | Access token TTL |
| `ULMS_FRONTEND_ORIGIN` | `http://localhost:5173` | CORS allow-list (single origin). Set to `http://localhost:3000` for the Next.js dev server if you ever add direct browser-to-Spring fetches; Server Action / Server Component traffic doesn't go through CORS. |

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

## Frontend

The frontend lives at `src/web/ulms-web/` and uses Next.js 16 (App Router, Server Actions), React 19, Tailwind 4, and shadcn/ui (`base-nova` style). The JWT returned by `POST /api/auth/login` is stored in an httpOnly cookie set by the Next.js Server Action — the browser never sees it in JS. Every backend call goes through the Next.js Node server (Server Components / Server Actions), so CORS is irrelevant for our own pages.

Phase 5 ships the student-facing surface: catalog browse + search (UC4), book detail with borrow / reserve (UC5/UC7), `/my/loans` + renew (UC9), `/my/reservations` + cancel (UC6), `/my/fines` + pay-via-gateway (UC10), `/my/notifications` + acknowledge (UC3). The navbar adds a notifications bell with unread badge and a "My library" dropdown for STUDENT users.

Phase 6 ships the librarian surface: `/librarian/catalog` (UC11 list / create / edit / delete with friendly 409 mapping) and `/librarian/returns` (UC8 lookup by loan id, confirm return). The navbar gains a "Librarian" dropdown for LIBRARIAN / ADMIN users.

Phase 7 ships the admin surface (UC12) plus EN/RO i18n and an a11y pass: `/admin/users` (paginated list with role filter), `/admin/users/new`, `/admin/users/[id]/edit`, and `/admin/users/[id]` with Profile / Loans / Fines tabs; activate / deactivate via AlertDialog. The navbar grows an "Admin" dropdown (ADMIN only) plus a small EN / RO locale switcher (cookie-persisted via next-intl). The skip-to-main link, table captions, and `aria-describedby` wiring round out the accessibility audit; axe-core jsdom assertions ship as Vitest specs.

Useful scripts (run from `src/web/ulms-web/`):

| Script | What it does |
|---|---|
| `npm run dev` | Next.js dev server on `:3000` |
| `npm run build` | Production build (`output: 'standalone'`) |
| `npm run lint` | ESLint |
| `npm run types:api` | Regenerate `src/lib/api/types.ts` from `http://localhost:8080/v3/api-docs`. Run after backend DTO/contract changes. |
| `npm test` | Vitest unit tests (auth + every Phase 5 API helper + `format.ts`) |
| `npm run test:e2e` | Playwright smoke tests (`e2e/auth-flow.spec.ts` + `e2e/student-flow.spec.ts` + `e2e/librarian-flow.spec.ts` + `e2e/admin-flow.spec.ts`). Requires the Java backend on `:8080`; cleanly skips if it's down. The student spec registers a fresh STUDENT per run; the librarian and admin specs use the seeded admin (no public librarian-registration endpoint exists, and ADMIN satisfies the same role guard). |
| `npm run verify` | `lint && build && test` (no e2e — backend-free) |

`.env.example` documents the only configurable knob (`NEXT_PUBLIC_API_URL=http://localhost:8080`).

## Tests

```sh
cd src/ulms
./mvnw test
```

H2 with PostgreSQL mode is the test datasource; the seed admin migration has an H2-flavoured mirror under `src/test/resources/db/migration-h2`.

For the frontend, see the *Frontend* section above (`npm test` for unit, `npm run test:e2e` for the Playwright smoke flow).
