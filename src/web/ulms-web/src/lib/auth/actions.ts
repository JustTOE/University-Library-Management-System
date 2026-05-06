"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { ApiError } from "@/lib/api/client";
import { login, register } from "@/lib/api/auth";

import { SESSION_COOKIE } from "./session";

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

const SESSION_MAX_AGE_SECONDS = 60 * 60; // matches backend JWT lifetime

function safeRedirectTarget(input: FormDataEntryValue | null): string {
  if (typeof input !== "string" || input.length === 0) return "/catalog";
  // Only allow same-origin paths beginning with a single forward slash.
  if (!input.startsWith("/") || input.startsWith("//")) return "/catalog";
  return input;
}

/* -------------------------------------------------------------------------- */
/* Login                                                                       */
/* -------------------------------------------------------------------------- */

export async function loginAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const email = String(formData.get("email") ?? "");
  const password = String(formData.get("password") ?? "");
  const redirectTo = safeRedirectTarget(formData.get("redirectTo"));

  let result;
  try {
    result = await login({ email, password });
  } catch (error) {
    return apiErrorToActionState(error, "Could not sign you in.");
  }

  if (!result.token) {
    return {
      status: "error",
      message: "Backend returned no token. Try again.",
    };
  }

  const store = await cookies();
  store.set(SESSION_COOKIE, result.token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: SESSION_MAX_AGE_SECONDS,
  });

  redirect(redirectTo);
}

/* -------------------------------------------------------------------------- */
/* Register                                                                    */
/* -------------------------------------------------------------------------- */

export async function registerAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const name = String(formData.get("name") ?? "");
  const email = String(formData.get("email") ?? "");
  const universityId = String(formData.get("universityId") ?? "");
  const phone = String(formData.get("phone") ?? "");
  const password = String(formData.get("password") ?? "");

  try {
    await register({
      name,
      email,
      universityId,
      phone: phone || undefined,
      password,
    });
  } catch (error) {
    return apiErrorToActionState(error, "Could not create your account.");
  }

  redirect("/login?registered=1");
}

/* -------------------------------------------------------------------------- */
/* Logout                                                                      */
/* -------------------------------------------------------------------------- */

export async function logoutAction(): Promise<void> {
  const store = await cookies();
  store.delete(SESSION_COOKIE);
  redirect("/login");
}

/* -------------------------------------------------------------------------- */
/* Helpers                                                                     */
/* -------------------------------------------------------------------------- */

function apiErrorToActionState(error: unknown, fallback: string): ActionState {
  if (error instanceof ApiError) {
    return {
      status: "error",
      message: error.message || fallback,
      fieldErrors: error.fieldErrors,
      lockedUntil: error.lockedUntil,
      retryAfter: error.retryAfter,
    };
  }
  // Re-throw redirect errors so Next.js can process them.
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
