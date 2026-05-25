import { NextResponse, type NextRequest } from "next/server";

import { SESSION_COOKIE } from "@/lib/auth/session";

/**
 * Next.js 16 proxy (formerly "middleware"): redirect unauthenticated requests
 * for the app surface to /login. Does NOT decode the JWT — that happens
 * server-side in (app)/layout.tsx via /api/auth/me. The proxy only checks
 * for the cookie's presence so it can run on the Edge runtime without
 * pulling in jose / jsonwebtoken.
 *
 * The forwarded request also gains an `x-current-path` header carrying the
 * original pathname + search; (app)/layout.tsx reads it to wire `requireAuth`'s
 * redirectTo at the actually-requested URL instead of always falling back to
 * /catalog.
 *
 * Public routes (login, register, logout, root, _next, favicon, assets)
 * are excluded by the matcher below.
 */
export function proxy(request: NextRequest) {
  const token = request.cookies.get(SESSION_COOKIE)?.value;
  const fullPath = `${request.nextUrl.pathname}${request.nextUrl.search}`;

  // The root path is public: it serves the landing page to guests and
  // redirects authenticated users to /catalog (handled in app/page.tsx).
  if (request.nextUrl.pathname === "/") {
    return NextResponse.next();
  }

  if (token) {
    const headers = new Headers(request.headers);
    headers.set("x-current-path", fullPath);
    return NextResponse.next({ request: { headers } });
  }

  const url = request.nextUrl.clone();
  url.pathname = "/login";
  url.search = `?redirectTo=${encodeURIComponent(fullPath)}`;
  return NextResponse.redirect(url);
}

export const config = {
  matcher: [
    "/((?!login|register|logout|_next|favicon\\.ico|api|.*\\.(?:svg|png|jpg|jpeg|gif|webp|ico|css|js|map)$).*)",
  ],
};
