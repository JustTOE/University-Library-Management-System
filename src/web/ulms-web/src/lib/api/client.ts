import "server-only";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

/**
 * Error envelope returned by ULMS Spring backend's GlobalExceptionHandler.
 * - 400 validation: includes `fieldErrors`.
 * - 402 payment declined: includes `declineReason`.
 * - 423 account locked: includes `lockedUntil` and a `Retry-After` header.
 */
export class ApiError extends Error {
  status: number;
  fieldErrors?: Record<string, string>;
  declineReason?: string;
  lockedUntil?: string;
  retryAfter?: number;

  constructor(
    status: number,
    message: string,
    extras: {
      fieldErrors?: Record<string, string>;
      declineReason?: string;
      lockedUntil?: string;
      retryAfter?: number;
    } = {},
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = extras.fieldErrors;
    this.declineReason = extras.declineReason;
    this.lockedUntil = extras.lockedUntil;
    this.retryAfter = extras.retryAfter;
  }
}

export type FetchOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  token?: string | null;
  query?: Record<string, string | number | boolean | undefined | null>;
  /** Override the global fetch (for tests). Defaults to globalThis.fetch. */
  fetchImpl?: typeof fetch;
};

/**
 * Server-only typed fetch wrapper. Always runs on the Next.js Node server —
 * the browser never sees the JWT.
 *
 * Throws ApiError on any non-2xx response or network failure.
 */
export async function apiFetch<T>(
  path: string,
  opts: FetchOptions = {},
): Promise<T> {
  const url = new URL(path, API_URL);
  if (opts.query) {
    for (const [key, value] of Object.entries(opts.query)) {
      if (value !== undefined && value !== null) {
        url.searchParams.set(key, String(value));
      }
    }
  }

  const headers: Record<string, string> = { Accept: "application/json" };
  if (opts.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (opts.token) {
    headers.Authorization = `Bearer ${opts.token}`;
  }

  const fetchImpl = opts.fetchImpl ?? globalThis.fetch;

  let response: Response;
  try {
    response = await fetchImpl(url, {
      method: opts.method ?? "GET",
      headers,
      body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
      cache: "no-store",
    });
  } catch {
    throw new ApiError(503, "Backend unavailable", {});
  }

  if (response.ok) {
    if (response.status === 204) return undefined as T;
    return (await response.json()) as T;
  }

  let body: Record<string, unknown> = {};
  try {
    body = (await response.json()) as Record<string, unknown>;
  } catch {
    // Non-JSON error body: fall through with empty extras.
  }

  const message =
    typeof body.message === "string" ? body.message : response.statusText;
  const fieldErrors =
    body.fieldErrors && typeof body.fieldErrors === "object"
      ? (body.fieldErrors as Record<string, string>)
      : undefined;
  const declineReason =
    typeof body.declineReason === "string" ? body.declineReason : undefined;
  const lockedUntil =
    typeof body.lockedUntil === "string" ? body.lockedUntil : undefined;
  const retryAfterHeader = response.headers.get("Retry-After");
  const retryAfter =
    retryAfterHeader && !Number.isNaN(Number(retryAfterHeader))
      ? Number(retryAfterHeader)
      : undefined;

  throw new ApiError(response.status, message, {
    fieldErrors,
    declineReason,
    lockedUntil,
    retryAfter,
  });
}
