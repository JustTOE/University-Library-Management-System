# ULMS web (Next.js + shadcn/ui)

Frontend for the University Library Management System. Next.js 16 (App Router, Server Actions), React 19, TypeScript, Tailwind 4, shadcn/ui (`base-nova` style).

## Quick start

Backend prerequisite: the Spring Boot service at `http://localhost:8080`. See the repo root README for how to start Postgres + the JVM. Once the backend's `/v3/api-docs` is reachable:

```sh
cp .env.example .env.local
npm install
npm run dev
```

Open `http://localhost:3000` and sign in with the seeded admin (`admin@ulms.local` / `admin-change-me-now`).

## Environment

| Variable | Default | Purpose |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | Server-side fetch base URL. Read by `src/lib/api/client.ts`. The browser never makes direct calls to this URL. |

## Auth shape

- JWT returned by `POST /api/auth/login` is stored in an httpOnly cookie named `ulms_session` (Server Action sets it; the Spring backend just returns the token in the response body).
- Browser never sees the token in JS.
- `proxy.ts` (Next.js 16's "middleware") redirects unauthenticated requests for `(app)` routes to `/login?redirectTo=…`. It does NOT decode the JWT — role checks live in `src/app/(app)/layout.tsx` via `requireAuth()` / `requireRole()`.
- Every backend call goes through the Next.js Node server (Server Components / Server Actions). CORS is irrelevant for our own pages.

## Scripts

| Script | What it does |
|---|---|
| `npm run dev` | Dev server on `:3000` |
| `npm run build` | Production build (`output: 'standalone'`) |
| `npm run start` | Run the standalone build |
| `npm run lint` | ESLint |
| `npm run types:api` | Regenerate `src/lib/api/types.ts` from `http://localhost:8080/v3/api-docs`. Run after backend DTO/contract changes. |
| `npm test` | Vitest unit tests |
| `npm run test:e2e` | Playwright smoke test (needs backend running). Skips cleanly if `/v3/api-docs` is unreachable. |
| `npm run test:e2e:ui` | Playwright in UI mode for debugging |
| `npm run verify` | `lint && build && test` (no e2e — backend-free) |

## Layout

```
src/
├ app/                       # App Router routes
│  ├ layout.tsx               # root: fonts, metadata
│  ├ page.tsx                 # / → /catalog or /login depending on session
│  ├ error.tsx                # client error boundary
│  ├ not-found.tsx            # 404
│  ├ (auth)/                  # unauthenticated surface
│  │  ├ layout.tsx            # centered Card; bounces authed users to /catalog
│  │  ├ login/page.tsx
│  │  └ register/page.tsx
│  └ (app)/                   # authenticated surface
│     ├ layout.tsx            # requireAuth + Navbar
│     └ catalog/page.tsx      # Phase 4 stub; Phase 5 fills in
├ components/
│  ├ ui/                      # shadcn primitives (button, card, input, label,
│  │                          # field, sonner, dropdown-menu, avatar, separator)
│  ├ auth/                    # login/register/logout client components
│  └ layout/                  # navbar
├ lib/
│  ├ api/                     # generated types + typed fetch helpers
│  └ auth/                    # session/actions/guards/state
├ proxy.ts                    # Next 16 "middleware" rename
└ ...
```
