import "server-only";

import type { CreateUserRequest, UpdateUserRequest } from "@/lib/api/users";

function parseOptionalString(raw: FormDataEntryValue | null): string | undefined {
  if (raw === null) return undefined;
  const value = String(raw).trim();
  return value === "" ? undefined : value;
}

function parseRequiredString(raw: FormDataEntryValue | null): string {
  return String(raw ?? "").trim();
}

function parseRole(raw: FormDataEntryValue | null): CreateUserRequest["role"] {
  const value = String(raw ?? "").trim();
  if (value === "STUDENT" || value === "LIBRARIAN" || value === "ADMIN") {
    return value;
  }
  return "STUDENT";
}

/**
 * Builds the CreateUserRequest body from a FormData payload. On edit, a blank
 * password field is omitted entirely so the backend keeps the existing hash
 * (the PUT /api/users/{id} update path treats a missing password as "unchanged"
 * and only re-hashes when a new value is supplied). On create the password is
 * always sent and is required server-side.
 */
export function readUserFormBody(formData: FormData, mode: "create"): CreateUserRequest;
export function readUserFormBody(formData: FormData, mode: "edit"): UpdateUserRequest;
export function readUserFormBody(
  formData: FormData,
  mode: "create" | "edit",
): CreateUserRequest | UpdateUserRequest {
  const passwordRaw = String(formData.get("password") ?? "").trim();
  const includePassword = mode === "create" || passwordRaw !== "";
  return {
    name: parseRequiredString(formData.get("name")),
    email: parseRequiredString(formData.get("email")),
    universityId: parseOptionalString(formData.get("universityId")),
    staffId: parseOptionalString(formData.get("staffId")),
    phone: parseOptionalString(formData.get("phone")),
    role: parseRole(formData.get("role")),
    ...(includePassword ? { password: passwordRaw } : {}),
  };
}
