import "server-only";

import { ApiError } from "./client";
import type { ActionState } from "./action-state";

/**
 * Maps a thrown ApiError (or unknown error) into an ActionState the UI can
 * render. Re-throws Next.js redirect errors so the framework can process them.
 *
 * Server Actions wrap their backend calls in try/catch and return the
 * ActionState produced here. Lives in a non-'use server' file so it can
 * return synchronously and be imported from any feature's actions.ts.
 */
export function apiErrorToActionState(
  error: unknown,
  fallback: string,
): ActionState {
  if (error instanceof ApiError) {
    return {
      status: "error",
      message: error.message || fallback,
      fieldErrors: error.fieldErrors,
      lockedUntil: error.lockedUntil,
      retryAfter: error.retryAfter,
      declineReason: error.declineReason,
    };
  }
  if (
    error &&
    typeof error === "object" &&
    "digest" in error &&
    typeof (error as { digest: unknown }).digest === "string" &&
    (error as { digest: string }).digest.startsWith("NEXT_REDIRECT")
  ) {
    throw error;
  }
  return { status: "error", message: fallback };
}
