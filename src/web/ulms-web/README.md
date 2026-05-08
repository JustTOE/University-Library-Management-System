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
- `proxy.ts` (Next.js 16's "middleware") redirects unauthenticated requests for `(app)` routes to `/login?redirectTo=…` and forwards the original URL to the layout via an `x-current-path` header so post-login redirects target the requested page. It does NOT decode the JWT — role checks live in `src/app/(app)/layout.tsx` via `requireAuth()` / `requireRole()`.
- Every backend call goes through the Next.js Node server (Server Components / Server Actions). CORS is irrelevant for our own pages.
- The SpEL on borrow / reserve / payment requires the body `userId` to match the JWT principal. Server Actions read `session.user.id` server-side and never trust client-supplied IDs.

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
│     ├ layout.tsx            # requireAuth + Navbar (with token) + Toaster
│     ├ catalog/
│     │  ├ page.tsx           # UC4 browse + search (URL-driven)
│     │  └ [id]/              # UC5 reserve, UC7 borrow
│     │     ├ page.tsx
│     │     ├ actions.ts      # borrowAction, reserveAction
│     │     ├ state.ts
│     │     ├ loading.tsx
│     │     └ not-found.tsx
│     ├ my/                   # STUDENT-only sub-surface
│     │  ├ layout.tsx         # requireRole(STUDENT) + sub-nav
│     │  ├ loans/             # UC9 my-loans + renew
│     │  ├ reservations/      # UC6 cancel reservation
│     │  ├ fines/             # UC10 pay-fine via Dialog (402 inline alert)
│     │  └ notifications/     # UC3 acknowledge
│     ├ librarian/            # LIBRARIAN/ADMIN sub-surface
│     │  ├ layout.tsx         # requireRole(LIBRARIAN, ADMIN) + sub-nav
│     │  ├ catalog/           # UC11 list + create + edit + delete
│     │  └ returns/           # UC8 process return by loan id
│     └ admin/                # ADMIN-only sub-surface
│        ├ layout.tsx         # requireRole(ADMIN) + sub-nav
│        └ users/             # UC12 list + create + edit + detail (Profile/Loans/Fines tabs)
├ components/
│  ├ ui/                      # shadcn primitives (12 added in 5b)
│  ├ auth/                    # login/register/logout client components
│  ├ catalog/                 # search-form, table, pagination, book-actions
│  ├ common/                  # status-pill, empty-state, error-alert
│  ├ my/                      # renew, cancel-reservation, pay-fine, acknowledge
│  ├ librarian/               # librarian-catalog-table, book-form, delete-book, confirm-return
│  ├ admin/                   # user-table, user-form, user-search-form, user-history-loans/fines, active-toggle, delete-user
│  └ layout/                  # navbar (bell + My library + Librarian + Admin dropdowns), skip-link
├ lib/
│  ├ api/                     # generated types + typed fetch helpers per resource
│  ├ auth/                    # session/actions/guards/state
│  ├ hooks/                   # use-toast-effect (shared transition→toast plumbing)
│  └ format.ts                # date-fns + Intl.NumberFormat helpers
├ i18n/                       # next-intl request config + locale type (English-only today)
├ messages/                   # en.json (single catalog)
├ proxy.ts                    # Next 16 "middleware" rename; sets x-current-path
└ ...
```

## Phase 5 surface

- **/catalog** + **/catalog/[id]** — UC4 browse/search and UC5/UC7 detail with Borrow / Reserve.
- **/my/loans** — UC9 list + renew.
- **/my/reservations** — UC6 list + cancel.
- **/my/fines** — UC10 outstanding balance + pay-fine dialog (gateway path; 402 keeps dialog open with inline decline reason).
- **/my/notifications** — UC3 list + acknowledge; opening `/my/notifications/[id]` auto-marks-as-read.

Navbar (STUDENT only): Catalog · Bell with unread badge · "My library" dropdown · avatar.

## Phase 6 surface

- **/librarian/catalog** — UC11 manage the catalog: search, paginate, edit, delete (handles 409 BookInUse).
- **/librarian/catalog/new** — UC11 add a new book; per-field validation; 409 ISBN duplicates surface inline next to the ISBN input.
- **/librarian/catalog/[id]/edit** — UC11 update a book (calls `notFound()` on a missing id).
- **/librarian/returns** — UC8 process a return: search by loan id, confirm via Server Action, the system handles fines (nightly job) and reservation fulfilment (notification) asynchronously.

Navbar (LIBRARIAN / ADMIN): Catalog · "Librarian" dropdown (Catalog / Returns) · avatar. The seeded admin satisfies the `or hasRole('ADMIN')` half of every Phase 6 endpoint guard, so it can drive the full surface end-to-end without a fresh librarian registration.

## Phase 7 surface

- **/admin/users** — UC12 list every user (client-side paginated over the full `GET /api/users` response, search by name/email/identifier + role filter); New user button opens `/admin/users/new`. Each row offers View / Edit / Activate-or-Deactivate (AlertDialog confirm) / Delete (AlertDialog confirm).
- **/admin/users/new** — UC12 create form; 409 unique-constraint conflicts (email / universityId / staffId) are rewritten into a friendly inline `fieldErrors.email`.
- **/admin/users/[id]** — Profile + Loans + Fines tabs. The history tab fetches `/api/users/{id}/history` once and renders both lists.
- **/admin/users/[id]/edit** — UC12 update form; password field accepts blank to keep the existing hashed password.

Backend exposes `isActive` on `UserResponse` (record extra positional field) so the admin UI can render Active / Inactive badges and the right toggle copy.

Navbar (ADMIN): an additional "Admin" dropdown (Users) appears alongside the Librarian dropdown.

i18n plumbing via `next-intl` 4.11 in i18n-without-routing mode is wired but the project ships English-only — `src/messages/en.json` is the single catalog and `LOCALES` in `src/i18n/config.ts` lists only `"en"`. The `t()` call sites across the navbar + admin surface keep the architecture in place if a second locale is ever added; today there is no user-facing locale switch.

Accessibility: skip-to-main link in the root layout, `<main id="main">` landmarks in (auth)/(app) layouts, table captions on every catalog/user/history table, `aria-describedby` on inputs that have a `<FieldDescription>`, focus-visible rings on navbar links. Component-level axe-core jsdom assertions ship as Vitest specs in `src/__tests__/a11y/`.
