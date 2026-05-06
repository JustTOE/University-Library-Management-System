import { afterEach, describe, expect, it, vi } from "vitest";

import {
  cancelReservation,
  createReservation,
  getReservationById,
  listReservationsByUser,
} from "./reservations";

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("createReservation()", () => {
  it("POSTs the ReservationRequest and returns the ReservationResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        id: 9,
        userId: 5,
        bookId: 3,
        status: "ACTIVE",
        queuePosition: 2,
      }),
    );

    const reservation = await createReservation(
      { userId: 5, bookId: 3 },
      { fetchImpl },
    );

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/reservations$/);
    expect(init?.method).toBe("POST");
    expect(JSON.parse(init?.body as string)).toEqual({ userId: 5, bookId: 3 });
    expect(reservation.queuePosition).toBe(2);
  });

  it("surfaces ApiError(409) for DataIntegrity (duplicate active reservation)", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(409, {
        status: 409,
        error: "Conflict",
        message: "This operation conflicts with existing data.",
      }),
    );

    await expect(
      createReservation({ userId: 5, bookId: 3 }, { fetchImpl }),
    ).rejects.toMatchObject({ status: 409 });
  });
});

describe("cancelReservation()", () => {
  it("DELETEs /api/reservations/{id} and returns the cancelled record", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 9, status: "CANCELLED" }),
    );

    const reservation = await cancelReservation(9, { fetchImpl });
    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/reservations\/9$/);
    expect(init?.method).toBe("DELETE");
    expect(reservation.status).toBe("CANCELLED");
  });
});

describe("listReservationsByUser() / getReservationById()", () => {
  it("listReservationsByUser returns the array", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(jsonResponse(200, []));
    const list = await listReservationsByUser(5, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/reservations\/user\/5$/);
    expect(list).toEqual([]);
  });

  it("getReservationById returns the ReservationResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 9, status: "ACTIVE", queuePosition: 1 }),
    );
    const r = await getReservationById(9, { fetchImpl });
    expect(r.id).toBe(9);
    expect(r.queuePosition).toBe(1);
  });
});
