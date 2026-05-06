/**
 * Pure types + initial state shared between Server Actions and the client
 * components that consume them via useActionState. Lives in its own file
 * (no "server-only" import) so client bundles can read the type without
 * pulling in server-only modules.
 */

export type ActionState =
  | { status: "idle" }
  | {
      status: "error";
      message: string;
      fieldErrors?: Record<string, string>;
      lockedUntil?: string;
      retryAfter?: number;
      declineReason?: string;
    }
  | { status: "success"; message: string };

export const initialActionState: ActionState = { status: "idle" };
