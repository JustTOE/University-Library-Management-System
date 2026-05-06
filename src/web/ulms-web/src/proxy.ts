import { NextResponse, type NextRequest } from "next/server";

import { SESSION_COOKIE } from "@/lib/auth/session";

/**
 * Next.js 16 proxy (formerly "middleware"): redirect unauthenticated requests
 * for the app surface to /login. Does NOT decode the JWT — that happens
 * server-side in (app)/layout.tsx via /api/auth/me. The proxy only checks
 * for the cookie's presence so it can run on the Edge runtime without
 * pulling in jose / jsonwebtoken.
 *
 * Public routes (login, register, logout, root, _next, favicon, assets)
 * are excluded by the matcher below.
 */
export function proxy(request: NextRequest) {
  const token = request.cookies.get(SESSION_COOKIE)?.value;
  if (token) return NextResponse.next();

  const url = request.nextUrl.clone();
  const redirectTo = `${url.pathname}${url.search}`;
  url.pathname = "/login";
  url.search = `?redirectTo=${encodeURIComponent(redirectTo)}`;
  return NextResponse.redirect(url);
}

export const config = {
  matcher: [
    "/((?!login|register|logout|_next|favicon\\.ico|api|.*\\.(?:svg|png|jpg|jpeg|gif|webp|ico|css|js|map)$).*)",
  ],
};
