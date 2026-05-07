import "server-only";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import { ApiError } from "@/lib/api/client";

import type { ActionState } from "./state";

/**
 * Maps a thrown ApiError to an ActionState for user create/update flows.
 * 409s without per-field detail are rewritten as a friendly fieldErrors.email
 * so the conflict surfaces inline next to the email input (the most common
 * cause: unique-email constraint violation).
 */
export function mapUserError(error: unknown, fallback: string): ActionState {
  if (
    error instanceof ApiError &&
    error.status === 409 &&
    !error.fieldErrors?.email &&
    !error.fieldErrors?.universityId &&
    !error.fieldErrors?.staffId
  ) {
    return {
      status: "error",
      message: fallback,
      fieldErrors: {
        ...(error.fieldErrors ?? {}),
        email: "Email, university ID, or staff ID is already in use.",
      },
    };
  }
  return apiErrorToActionState(error, fallback);
}
