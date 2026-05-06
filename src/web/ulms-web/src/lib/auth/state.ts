/**
 * Pure types + initial state for the auth Server Actions. Lives in its own
 * file because a 'use server' module can only export async functions —
 * exporting a constant or a type would crash Next.js at runtime with
 * "A 'use server' file can only export async functions, found object."
 */

export type ActionState =
  | { status: "idle" }
  | {
      status: "error";
      message: string;
      fieldErrors?: Record<string, string>;
      lockedUntil?: string;
      retryAfter?: number;
    }
  | { status: "success"; message: string };

export const initialActionState: ActionState = { status: "idle" };
