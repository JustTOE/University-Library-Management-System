import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiError } from "./client";
import { listPaymentsByUser, processPayment } from "./payments";

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("processPayment()", () => {
  it("POSTs the PaymentRequest and returns COMPLETED on success", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        id: 1,
        fineId: 2,
        userId: 5,
        amount: 5,
        method: "CARD",
        status: "COMPLETED",
      }),
    );

    const payment = await processPayment(
      { fineId: 2, userId: 5, method: "CARD" },
      { fetchImpl },
    );

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/payments$/);
    expect(init?.method).toBe("POST");
    expect(JSON.parse(init?.body as string)).toEqual({
      fineId: 2,
      userId: 5,
      method: "CARD",
    });
    expect(payment.status).toBe("COMPLETED");
  });

  it("extracts declineReason on a 402 PaymentDeclined", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(402, {
        status: 402,
        error: "Payment Required",
        message: "Payment was declined",
        declineReason: "Cash payment exceeds €100 limit",
      }),
    );

    try {
      await processPayment(
        { fineId: 2, userId: 5, method: "CASH" },
        { fetchImpl },
      );
      expect.fail("expected ApiError");
    } catch (caught) {
      expect(caught).toBeInstanceOf(ApiError);
      const err = caught as ApiError;
      expect(err.status).toBe(402);
      expect(err.declineReason).toBe("Cash payment exceeds €100 limit");
    }
  });
});

describe("listPaymentsByUser()", () => {
  it("returns the payments array", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(jsonResponse(200, []));
    await listPaymentsByUser(5, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/payments\/user\/5$/);
  });
});
