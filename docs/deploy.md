# ULMS Deployment Guide

This document covers how to run ULMS in three modes — local development, fully containerised local dev, and a single-host production deployment behind Caddy + Let's Encrypt.

## Topology

```
                ┌─────────────────────────────────────────────────────┐
                │ docker-compose.prod.yml (single VPS)                │
                │                                                     │
   browser ───► caddy (80/443, ACME)                                  │
                │    ├── / ───────────► frontend (Next.js standalone) │
                │    └── /api/*, /v3/api-docs, /actuator/health ─────►│
                │                       backend (Spring Boot)         │
                │                              ▼                      │
                │                       postgres ◄── backup (pg_dump) │
                └─────────────────────────────────────────────────────┘
```

Containers communicate over the private `ulms` bridge network. Only Caddy exposes ports to the host (80, 443). Postgres, the backend, and the frontend have no host port mappings in prod — they're reachable only through Caddy.

## Local development (no containers for app code)

```sh
docker compose up -d postgres_database mailhog
cd src/ulms
JWT_SECRET=dev-only-jwt-secret-for-local-32-byte-min ./mvnw spring-boot:run
# in another terminal:
cd src/web/ulms-web
npm install
npm run dev
```

Frontend on `http://localhost:3000`, backend on `http://localhost:8080`, MailHog UI on `http://localhost:8025`. The seeded admin (`admin@ulms.local` / `admin-change-me-now`, migration `V13`) lets you sign in immediately.

## Local development (everything in compose)

`docker-compose.yml` brings up Postgres, MailHog, and the backend container (built from `src/ulms/Dockerfile`). It's useful for spot-checking the production-style image against a friendly dev database; the Next.js frontend is **not** included here — keep running it with `npm run dev` so hot reload still works.

```sh
JWT_SECRET=dev-only-jwt-secret-for-local-32-byte-min docker compose up -d --build
```

The backend's healthcheck (`HEALTHCHECK CMD curl /actuator/health`) gates the container "healthy" state; `docker compose ps` will show this once migrations have applied and the scheduler is wired up (~30 s on a cold image).

## Production

### Prerequisites

- A host with Docker 24+ and `docker compose` v2.
- A DNS A record pointing your domain to the host's public IP.
- Port 80 + 443 open on the host firewall (Caddy needs both for the HTTP-01 challenge).
- ≥ 1 GB RAM free (the JVM defaults to 75% of the container memory; tune `JAVA_OPTS` to fit smaller VPSes).

### One-time setup

```sh
git clone <repo> /opt/ulms
cd /opt/ulms
cp .env.prod.example .env.prod
$EDITOR .env.prod
```

`.env.prod` is gitignored. Required values:

| Variable | Notes |
|---|---|
| `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB` | DB credentials. Generate a strong password. |
| `JWT_SECRET` | ≥ 32 bytes. `openssl rand -base64 48` is a sensible source. **Rotating invalidates every active session.** |
| `ULMS_FRONTEND_ORIGIN` | Must equal `https://${ULMS_DOMAIN}` — the backend's CORS allow-list keys off this string. |
| `ULMS_DOMAIN` | Apex domain or subdomain (no scheme, no trailing slash). |
| `ULMS_ACME_EMAIL` | Used by Let's Encrypt for expiry reminders. |
| `SPRING_MAIL_HOST` etc. | A real SMTP relay. Sendgrid / Mailgun / SES all work; MailHog is **not** for production. Set `ULMS_MAIL_ENABLED=false` to disable mail entirely if you're not running a relay yet. |

### Bring the stack up

```sh
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

Caddy will provision a TLS certificate on first run; this usually completes in 30–60 s. Tail `docker compose logs -f caddy` if you don't see HTTPS responding within two minutes.

### Smoke checks

```sh
curl -sSf https://${ULMS_DOMAIN}/actuator/health | jq .
# {"status":"UP"}

curl -sSf https://${ULMS_DOMAIN}/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@ulms.local","password":"admin-change-me-now"}' | jq .role
# "ADMIN"
```

Then open `https://${ULMS_DOMAIN}` in a browser and sign in.

### Rotating the seeded admin password

The seeded admin password is **public knowledge** (`admin-change-me-now`, documented in `V13__seed_admin.sql`). Change it on the first reachable deployment:

1. Sign in as the admin.
2. Open the user editor at `/admin/users/{id}/edit`, enter a new password, save.

There is no force-reset gate yet; document the rotation in your run notes.

### Backups

The `backup` sidecar service runs `pg_dump` once per day and writes gzipped dumps to the `ulms-postgres-backups` named volume, keeping the last `BACKUP_RETENTION_DAYS` (default 7).

To inspect:

```sh
docker compose -f docker-compose.prod.yml exec backup ls -lh /backups
```

To restore from a dump (the database name is whatever you set in `.env.prod`):

```sh
docker compose -f docker-compose.prod.yml exec -T postgres \
  sh -c 'gunzip | psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}"' \
  < ./ulms-20260514T020000Z.sql.gz
```

If you need an off-host copy (recommended), schedule a cron on the host that `docker cp`'s the latest dump to your backup bucket / NAS:

```cron
30 3 * * * cd /opt/ulms && docker compose -f docker-compose.prod.yml cp backup:/backups/$(docker compose -f docker-compose.prod.yml exec -T backup ls -t /backups | head -1) /var/backups/ulms/
```

### Updating

```sh
git pull
docker compose -f docker-compose.prod.yml --env-file .env.prod build
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

Flyway migrations apply on backend boot. Roll-forward only: if you need to revert a schema change, write a new `Vn__revert_*.sql` migration rather than editing or deleting an existing one.

### Tearing it down

```sh
docker compose -f docker-compose.prod.yml --env-file .env.prod down       # keeps data
docker compose -f docker-compose.prod.yml --env-file .env.prod down -v    # WIPES Postgres + backups
```

## Troubleshooting

- **Backend stuck "starting" forever.** Check `JWT_SECRET` length — anything under 32 bytes makes the app refuse to start, and the healthcheck never goes green. Tail `docker compose logs backend`.
- **Caddy keeps requesting a cert and failing.** Verify DNS resolves to your host's public IP and that ports 80 + 443 are reachable. Let's Encrypt aggressively rate-limits repeated failures — fix the underlying issue (DNS, firewall) before retrying.
- **CORS errors in the browser.** `ULMS_FRONTEND_ORIGIN` in `.env.prod` must exactly equal the scheme + host the browser uses. `https://` ≠ `http://`, `ulms.example.com` ≠ `www.ulms.example.com`.
- **`spring.flyway.clean-disabled`.** The `prod` profile (`application-prod.properties`) locks this to `true`. Do not flip it; you'll lose every row in the database.

## File map

```
.env.prod.example          # template for production secrets
docker-compose.yml         # local dev: postgres + mailhog + (optional) containerised backend
docker-compose.prod.yml    # full prod stack: postgres + backend + frontend + caddy + backup
deploy/Caddyfile           # Caddy reverse-proxy config (TLS + headers + routing rules)
docs/deploy.md             # this file
src/ulms/Dockerfile        # backend multi-stage build (Temurin 25, non-root, healthcheck)
src/ulms/src/main/resources/application-prod.properties  # prod-profile Spring overrides
src/web/ulms-web/Dockerfile  # frontend multi-stage build (Node 22, standalone output, non-root)
```
