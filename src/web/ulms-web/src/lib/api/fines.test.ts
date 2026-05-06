import { afterEach, describe, expect, it, vi } from "vitest";

import {
  getFineById,
  getTotalUnpaid,
  listFinesByUser,
  listUnpaidFinesByUser,
} from "./fines";

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("listFinesByUser() / listUnpaidFinesByUser()", () => {
  it("listFinesByUser hits /api/fines/user/{id}", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, [{ id: 1, amount: 5, status: "UNPAID" }]),
    );
    const list = await listFinesByUser(5, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/fines\/user\/5$/);
    expect(list).toHaveLength(1);
  });

  it("listUnpaidFinesByUser hits /api/fines/user/{id}/unpaid", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(jsonResponse(200, []));
    await listUnpaidFinesByUser(5, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/fines\/user\/5\/unpaid$/);
  });
});

describe("getTotalUnpaid()", () => {
  it("returns the BigDecimal as a number", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      new Response("12.50", {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    const total = await getTotalUnpaid(5, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/fines\/user\/5\/total-unpaid$/);
    expect(total).toBe(12.5);
  });
});

describe("getFineById()", () => {
  it("returns the FineResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 7, amount: 5, status: "UNPAID" }),
    );

    const fine = await getFineById(7, { fetchImpl });
    expect(fine.id).toBe(7);
    expect(fine.status).toBe("UNPAID");
  });

  it("throws ApiError(404) on missing", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(404, {
        status: 404,
        error: "Not Found",
        message: "Fine not found: 99",
      }),
    );

    await expect(getFineById(99, { fetchImpl })).rejects.toMatchObject({
      status: 404,
    });
  });
});
