import { afterEach, describe, expect, it, vi } from "vitest";

import {
  acknowledgeNotification,
  getNotificationById,
  getUnreadCount,
  listNotificationsByUser,
} from "./notifications";

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("listNotificationsByUser()", () => {
  it("GETs /api/notifications/user/{id}", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, [
        { id: 1, message: "Due in 3 days", status: "NOT_ACKNOWLEDGED" },
      ]),
    );
    const list = await listNotificationsByUser(5, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/notifications\/user\/5$/);
    expect(list).toHaveLength(1);
  });
});

describe("getUnreadCount()", () => {
  it("returns the UnreadCountResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { count: 3 }),
    );
    const result = await getUnreadCount(5, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/notifications\/user\/5\/unread-count$/);
    expect(result.count).toBe(3);
  });
});

describe("getNotificationById() / acknowledgeNotification()", () => {
  it("getNotificationById returns the NotificationResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 1, status: "NOT_ACKNOWLEDGED" }),
    );
    await getNotificationById(1, { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/notifications\/1$/);
  });

  it("acknowledgeNotification PUTs /api/notifications/{id}/acknowledge", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 1, status: "ACKNOWLEDGED" }),
    );

    const updated = await acknowledgeNotification(1, { fetchImpl });
    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/notifications\/1\/acknowledge$/);
    expect(init?.method).toBe("PUT");
    expect(updated.status).toBe("ACKNOWLEDGED");
  });
});
