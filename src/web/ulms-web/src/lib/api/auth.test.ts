import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiError } from "./client";
import { login, register, me } from "./auth";

function jsonResponse(status: number, body: unknown, headers: HeadersInit = {}) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json", ...headers },
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("login()", () => {
  it("POSTs JSON and returns the parsed AuthResponse on 200", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        token: "jwt.value",
        expiresAt: "2026-05-06T13:00:00Z",
        role: "ADMIN",
        userId: 1,
        name: "Default Admin",
      }),
    );

    const result = await login(
      { email: "admin@ulms.local", password: "admin-change-me-now" },
      { fetchImpl },
    );

    expect(fetchImpl).toHaveBeenCalledOnce();
    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/auth\/login$/);
    expect(init?.method).toBe("POST");
    const headers = init?.headers as Record<string, string>;
    expect(headers["Content-Type"]).toBe("application/json");
    expect(JSON.parse(init?.body as string)).toEqual({
      email: "admin@ulms.local",
      password: "admin-change-me-now",
    });
    expect(result.token).toBe("jwt.value");
    expect(result.role).toBe("ADMIN");
  });

  it("throws ApiError(401) with the server message on bad credentials", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(401, {
        status: 401,
        error: "Unauthorized",
        message: "Invalid email or password",
      }),
    );

    await expect(
      login({ email: "x", password: "y" }, { fetchImpl }),
    ).rejects.toMatchObject({
      name: "ApiError",
      status: 401,
      message: "Invalid email or password",
    });
  });

  it("extracts lockedUntil and Retry-After on 423", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(
        423,
        {
          status: 423,
          error: "Locked",
          message: "Account locked due to too many failed login attempts",
          lockedUntil: "2026-05-06T13:15:00Z",
        },
        { "Retry-After": "900" },
      ),
    );

    try {
      await login({ email: "x", password: "y" }, { fetchImpl });
      expect.fail("expected ApiError");
    } catch (caught) {
      expect(caught).toBeInstanceOf(ApiError);
      const err = caught as ApiError;
      expect(err.status).toBe(423);
      expect(err.lockedUntil).toBe("2026-05-06T13:15:00Z");
      expect(err.retryAfter).toBe(900);
    }
  });

  it("surfaces validation fieldErrors on 400", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(400, {
        status: 400,
        error: "Bad Request",
        message: "Validation failed",
        fieldErrors: { email: "Email must be valid" },
      }),
    );

    try {
      await login({ email: "bad", password: "y" }, { fetchImpl });
      expect.fail("expected ApiError");
    } catch (caught) {
      const err = caught as ApiError;
      expect(err.status).toBe(400);
      expect(err.fieldErrors).toEqual({ email: "Email must be valid" });
    }
  });

  it("returns ApiError(503) on network failure", async () => {
    const fetchImpl = vi.fn().mockRejectedValue(new TypeError("fetch failed"));

    try {
      await login({ email: "x", password: "y" }, { fetchImpl });
      expect.fail("expected ApiError");
    } catch (caught) {
      const err = caught as ApiError;
      expect(err.status).toBe(503);
      expect(err.message).toBe("Backend unavailable");
    }
  });
});

describe("register()", () => {
  it("POSTs the RegisterRequest and returns UserResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(201, {
        id: 42,
        name: "Ada Lovelace",
        email: "ada@upb.ro",
        universityId: "UPB-2026-0042",
        role: "STUDENT",
      }),
    );

    const result = await register(
      {
        name: "Ada Lovelace",
        email: "ada@upb.ro",
        universityId: "UPB-2026-0042",
        password: "correcthorsebatterystaple",
      },
      { fetchImpl },
    );

    expect(result.id).toBe(42);
    expect(result.role).toBe("STUDENT");
  });
});

describe("me()", () => {
  it("attaches Bearer token and returns UserResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        id: 1,
        name: "Default Admin",
        email: "admin@ulms.local",
        universityId: "",
        role: "ADMIN",
      }),
    );

    const result = await me("the-jwt-token", { fetchImpl });

    const [, init] = fetchImpl.mock.calls[0]!;
    const headers = init?.headers as Record<string, string>;
    expect(headers.Authorization).toBe("Bearer the-jwt-token");
    expect(result.role).toBe("ADMIN");
  });

  it("throws ApiError(401) when the token is rejected", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(401, {
        status: 401,
        error: "Unauthorized",
        message: "Authentication required",
      }),
    );

    await expect(me("bad-token", { fetchImpl })).rejects.toMatchObject({
      status: 401,
    });
  });
});
