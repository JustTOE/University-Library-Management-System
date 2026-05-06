import "server-only";

import { cookies } from "next/headers";

import { ApiError } from "@/lib/api/client";
import { me, type UserResponse } from "@/lib/api/auth";

export const SESSION_COOKIE = "ulms_session";

/** Reads the JWT from the httpOnly session cookie, or null if absent. */
export async function readToken(): Promise<string | null> {
  const store = await cookies();
  return store.get(SESSION_COOKIE)?.value ?? null;
}

/**
 * Resolves the current user from the session cookie by calling /api/auth/me.
 * Returns null if the cookie is absent or the token is rejected (401/403).
 *
 * Side effect: clears the cookie on 401/403 so subsequent requests are
 * treated as anonymous without retrying the rejected token.
 */
export async function readSession(): Promise<{
  token: string;
  user: UserResponse;
} | null> {
  const token = await readToken();
  if (!token) return null;

  try {
    const user = await me(token);
    return { token, user };
  } catch (error) {
    if (
      error instanceof ApiError &&
      (error.status === 401 || error.status === 403)
    ) {
      const store = await cookies();
      store.delete(SESSION_COOKIE);
    }
    return null;
  }
}
