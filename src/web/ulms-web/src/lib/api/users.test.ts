import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiError } from "./client";
import {
  activateUser,
  createUser,
  deactivateUser,
  deleteUser,
  getUserById,
  getUserHistory,
  listUsers,
  updateUser,
} from "./users";

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function noContentResponse() {
  return new Response(null, { status: 204 });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("listUsers()", () => {
  it("GETs /api/users and returns the array", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, [
        { id: 1, name: "Admin", email: "a@x", role: "ADMIN", isActive: true },
      ]),
    );

    const users = await listUsers({ fetchImpl, token: "tok" });

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/users$/);
    expect(init?.method ?? "GET").toBe("GET");
    const headers = init?.headers as Record<string, string>;
    expect(headers.Authorization).toBe("Bearer tok");
    expect(users[0].isActive).toBe(true);
  });
});

describe("getUserById()", () => {
  it("GETs /api/users/{id}", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 7, name: "Bob", role: "STUDENT", isActive: false }),
    );

    const user = await getUserById(7, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/users\/7$/);
    expect(user.isActive).toBe(false);
  });
});

describe("createUser()", () => {
  it("POSTs the CreateUserRequest body", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 99, name: "New", role: "LIBRARIAN", isActive: true }),
    );

    await createUser(
      {
        name: "New",
        email: "new@x",
        role: "LIBRARIAN",
        password: "p@ss-12345",
        staffId: "S-1",
      },
      { fetchImpl },
    );

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/users$/);
    expect(init?.method).toBe("POST");
    const body = JSON.parse(init?.body as string);
    expect(body.role).toBe("LIBRARIAN");
    expect(body.staffId).toBe("S-1");
  });

  it("surfaces ApiError(409) when email is already used", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(409, {
        status: 409,
        error: "Conflict",
        message: "Email is already in use",
      }),
    );

    try {
      await createUser(
        {
          name: "Dup",
          email: "dup@x",
          role: "STUDENT",
          password: "p@ss-12345",
        },
        { fetchImpl },
      );
      expect.fail("expected ApiError");
    } catch (caught) {
      expect(caught).toBeInstanceOf(ApiError);
      expect((caught as ApiError).status).toBe(409);
    }
  });
});

describe("updateUser()", () => {
  it("PUTs /api/users/{id}", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 7, name: "Bob", role: "STUDENT", isActive: true }),
    );

    await updateUser(
      7,
      {
        name: "Bob",
        email: "bob@x",
        role: "STUDENT",
        password: "p@ss-12345",
      },
      { fetchImpl },
    );

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/users\/7$/);
    expect(init?.method).toBe("PUT");
  });
});

describe("activateUser() / deactivateUser()", () => {
  it("activate PUTs /api/users/{id}/activate", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 7, role: "STUDENT", isActive: true }),
    );

    const user = await activateUser(7, { fetchImpl });

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/users\/7\/activate$/);
    expect(init?.method).toBe("PUT");
    expect(user.isActive).toBe(true);
  });

  it("deactivate PUTs /api/users/{id}/deactivate", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 7, role: "STUDENT", isActive: false }),
    );

    const user = await deactivateUser(7, { fetchImpl });

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/users\/7\/deactivate$/);
    expect(init?.method).toBe("PUT");
    expect(user.isActive).toBe(false);
  });
});

describe("deleteUser()", () => {
  it("DELETEs and returns undefined for 204", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(noContentResponse());

    const result = await deleteUser(7, { fetchImpl });

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/users\/7$/);
    expect(init?.method).toBe("DELETE");
    expect(result).toBeUndefined();
  });

  it("surfaces ApiError(409) when the user is referenced by other rows", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(409, {
        status: 409,
        error: "Conflict",
        message: "This operation conflicts with existing data.",
      }),
    );

    await expect(deleteUser(7, { fetchImpl })).rejects.toMatchObject({
      name: "ApiError",
      status: 409,
    });
  });
});

describe("getUserHistory()", () => {
  it("GETs /api/users/{id}/history and returns loans + fines", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        loans: [{ id: 1, status: "ACTIVE" }],
        fines: [{ id: 2, status: "UNPAID" }],
      }),
    );

    const history = await getUserHistory(7, { fetchImpl });

    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/users\/7\/history$/);
    expect(history.loans?.[0]?.status).toBe("ACTIVE");
    expect(history.fines?.[0]?.status).toBe("UNPAID");
  });
});
