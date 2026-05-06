import "server-only";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import { ApiError } from "@/lib/api/client";

import type { ActionState } from "./state";

/**
 * Maps a thrown ApiError to an ActionState for book create/update flows.
 * 409s without a per-field ISBN error are rewritten as a friendly
 * fieldErrors.isbn so the UI renders the conflict next to the ISBN input.
 */
export function mapBookError(error: unknown, fallback: string): ActionState {
  if (
    error instanceof ApiError &&
    error.status === 409 &&
    !error.fieldErrors?.isbn
  ) {
    return {
      status: "error",
      message: fallback,
      fieldErrors: {
        ...(error.fieldErrors ?? {}),
        isbn: "A book with this ISBN already exists.",
      },
    };
  }
  return apiErrorToActionState(error, fallback);
}
