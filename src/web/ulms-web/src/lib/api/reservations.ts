import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";

export type ReservationRequest = components["schemas"]["ReservationRequest"];
export type ReservationResponse = components["schemas"]["ReservationResponse"];

export function createReservation(
  body: ReservationRequest,
  opts: FetchOptions = {},
) {
  return apiFetch<ReservationResponse>("/api/reservations", {
    ...opts,
    method: "POST",
    body,
  });
}

export function cancelReservation(id: number, opts: FetchOptions = {}) {
  return apiFetch<ReservationResponse>(`/api/reservations/${id}`, {
    ...opts,
    method: "DELETE",
  });
}

export function listReservationsByUser(
  userId: number,
  opts: FetchOptions = {},
) {
  return apiFetch<ReservationResponse[]>(
    `/api/reservations/user/${userId}`,
    opts,
  );
}

export function getReservationById(id: number, opts: FetchOptions = {}) {
  return apiFetch<ReservationResponse>(`/api/reservations/${id}`, opts);
}
