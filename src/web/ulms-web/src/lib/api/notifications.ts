import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";

export type NotificationResponse = components["schemas"]["NotificationResponse"];
export type UnreadCountResponse = components["schemas"]["UnreadCountResponse"];

export function listNotificationsByUser(
  userId: number,
  opts: FetchOptions = {},
) {
  return apiFetch<NotificationResponse[]>(
    `/api/notifications/user/${userId}`,
    opts,
  );
}

export function getUnreadCount(userId: number, opts: FetchOptions = {}) {
  return apiFetch<UnreadCountResponse>(
    `/api/notifications/user/${userId}/unread-count`,
    opts,
  );
}

export function getNotificationById(id: number, opts: FetchOptions = {}) {
  return apiFetch<NotificationResponse>(`/api/notifications/${id}`, opts);
}

export function acknowledgeNotification(id: number, opts: FetchOptions = {}) {
  return apiFetch<NotificationResponse>(
    `/api/notifications/${id}/acknowledge`,
    { ...opts, method: "PUT" },
  );
}
