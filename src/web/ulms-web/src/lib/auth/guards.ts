import "server-only";

import { redirect } from "next/navigation";

import type { UserRole, UserResponse } from "@/lib/api/auth";

import { readSession } from "./session";

/**
 * Server-side helper for `(app)/layout.tsx` and any role-gated layout.
 * Redirects to /login (with redirectTo) if no session.
 */
export async function requireAuth(currentPath: string): Promise<{
  token: string;
  user: UserResponse;
}> {
  const session = await readSession();
  if (!session) {
    const target = `/login?redirectTo=${encodeURIComponent(currentPath)}`;
    redirect(target);
  }
  return session;
}

/**
 * Like requireAuth, but additionally enforces that the user has one of the
 * given roles. Redirects to /catalog (a non-privileged landing page) on
 * mismatch — never falls through to a 403 mid-render.
 */
export async function requireRole(
  currentPath: string,
  ...allowed: UserRole[]
): Promise<{ token: string; user: UserResponse }> {
  const session = await requireAuth(currentPath);
  if (!session.user.role || !allowed.includes(session.user.role)) {
    redirect("/catalog");
  }
  return session;
}
