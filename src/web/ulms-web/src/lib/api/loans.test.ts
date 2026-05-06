import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiError } from "./client";
import {
  borrow,
  getLoanById,
  listLoansByUser,
  listLoansByUserPaged,
  renew,
  returnLoan,
} from "./loans";

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("borrow()", () => {
  it("POSTs the BorrowRequest and returns the LoanResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        id: 11,
        userId: 5,
        bookId: 3,
        status: "ACTIVE",
        dueDate: "2026-05-20",
      }),
    );

    const loan = await borrow({ userId: 5, bookId: 3 }, {
      fetchImpl,
      token: "tok",
    });

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/loans\/borrow$/);
    expect(init?.method).toBe("POST");
    expect(JSON.parse(init?.body as string)).toEqual({ userId: 5, bookId: 3 });
    expect(loan.status).toBe("ACTIVE");
  });

  it("surfaces ApiError(409) for BookNotAvailable", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(409, {
        status: 409,
        error: "Conflict",
        message: "Book has no copies available",
      }),
    );

    try {
      await borrow({ userId: 5, bookId: 3 }, { fetchImpl });
      expect.fail("expected ApiError");
    } catch (caught) {
      expect(caught).toBeInstanceOf(ApiError);
      const err = caught as ApiError;
      expect(err.status).toBe(409);
      expect(err.message).toBe("Book has no copies available");
    }
  });
});

describe("renew()", () => {
  it("PUTs /api/loans/{id}/renew and returns the LoanResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 11, status: "RENEWED", renewalCount: 1 }),
    );

    const loan = await renew(11, { fetchImpl });
    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/loans\/11\/renew$/);
    expect(init?.method).toBe("PUT");
    expect(loan.renewalCount).toBe(1);
  });

  it("surfaces ApiError(409) for LoanState (max renewals reached)", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(409, {
        status: 409,
        error: "Conflict",
        message: "Maximum renewals reached",
      }),
    );

    await expect(renew(11, { fetchImpl })).rejects.toMatchObject({
      status: 409,
      message: "Maximum renewals reached",
    });
  });
});

describe("listLoansByUser() / listLoansByUserPaged() / getLoanById()", () => {
  it("listLoansByUser returns the array", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(jsonResponse(200, []));
    const loans = await listLoansByUser(5, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/loans\/user\/5$/);
    expect(loans).toEqual([]);
  });

  it("listLoansByUserPaged threads paging into the URL", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { content: [], totalElements: 0, number: 0, size: 20 }),
    );
    await listLoansByUserPaged(5, { page: 2, size: 10, sort: "borrowDate,desc" }, {
      fetchImpl,
    });
    const [url] = fetchImpl.mock.calls[0]!;
    const u = new URL(String(url));
    expect(u.pathname).toBe("/api/loans/user/5/paged");
    expect(u.searchParams.get("page")).toBe("2");
    expect(u.searchParams.get("size")).toBe("10");
    expect(u.searchParams.get("sort")).toBe("borrowDate,desc");
  });

  it("getLoanById returns the LoanResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 11, status: "ACTIVE" }),
    );
    const loan = await getLoanById(11, { fetchImpl });
    expect(loan.id).toBe(11);
  });
});

describe("returnLoan()", () => {
  it("PUTs /api/loans/{id}/return and returns the LoanResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 11, status: "RETURNED" }),
    );

    const loan = await returnLoan(11, { fetchImpl, token: "tok" });

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/loans\/11\/return$/);
    expect(init?.method).toBe("PUT");
    const headers = init?.headers as Record<string, string>;
    expect(headers.Authorization).toBe("Bearer tok");
    expect(loan.status).toBe("RETURNED");
  });

  it("surfaces ApiError(409) when the loan is already returned", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(409, {
        status: 409,
        error: "Conflict",
        message: "Loan is already returned",
      }),
    );

    await expect(returnLoan(11, { fetchImpl })).rejects.toMatchObject({
      name: "ApiError",
      status: 409,
      message: "Loan is already returned",
    });
  });
});
