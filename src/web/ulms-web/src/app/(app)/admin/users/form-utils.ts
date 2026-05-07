import "server-only";

import type { CreateUserRequest } from "@/lib/api/users";

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
 * Builds the CreateUserRequest body from a FormData payload. On edit, when the
 * password field is left blank, we still send the empty string — the backend's
 * userService.save accepts it and skips re-hashing. (We could omit it via an
 * undefined value, but the OpenAPI contract types password as required, so an
 * empty string keeps the type happy without changing semantics on the JVM side.)
 */
export function readUserFormBody(formData: FormData, mode: "create" | "edit"): CreateUserRequest {
  const passwordRaw = String(formData.get("password") ?? "").trim();
  return {
    name: parseRequiredString(formData.get("name")),
    email: parseRequiredString(formData.get("email")),
    universityId: parseOptionalString(formData.get("universityId")),
    staffId: parseOptionalString(formData.get("staffId")),
    phone: parseOptionalString(formData.get("phone")),
    role: parseRole(formData.get("role")),
    password: mode === "create" ? passwordRaw : passwordRaw,
  };
}
