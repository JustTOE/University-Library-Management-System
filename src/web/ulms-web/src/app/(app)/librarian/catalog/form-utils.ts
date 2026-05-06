import "server-only";

import type { CreateBookRequest } from "@/lib/api/books";

function parseOptionalInt(raw: FormDataEntryValue | null): number | undefined {
  if (raw === null) return undefined;
  const value = String(raw).trim();
  if (!value) return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

function parseRequiredInt(raw: FormDataEntryValue | null): number {
  const value = String(raw ?? "").trim();
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function parseOptionalString(raw: FormDataEntryValue | null): string | undefined {
  if (raw === null) return undefined;
  const value = String(raw).trim();
  return value === "" ? undefined : value;
}

export function readBookFormBody(formData: FormData): CreateBookRequest {
  return {
    title: String(formData.get("title") ?? "").trim(),
    author: String(formData.get("author") ?? "").trim(),
    isbn: String(formData.get("isbn") ?? "").trim(),
    publicationYear: parseOptionalInt(formData.get("publicationYear")),
    subject: parseOptionalString(formData.get("subject")),
    totalCopies: parseRequiredInt(formData.get("totalCopies")),
    availableCopies: parseRequiredInt(formData.get("availableCopies")),
    shelfNumber: parseOptionalString(formData.get("shelfNumber")),
  };
}
