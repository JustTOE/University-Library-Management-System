import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";

export type FineResponse = components["schemas"]["FineResponse"];

export function listFinesByUser(userId: number, opts: FetchOptions = {}) {
  return apiFetch<FineResponse[]>(`/api/fines/user/${userId}`, opts);
}

export function listUnpaidFinesByUser(
  userId: number,
  opts: FetchOptions = {},
) {
  return apiFetch<FineResponse[]>(`/api/fines/user/${userId}/unpaid`, opts);
}

export function getTotalUnpaid(userId: number, opts: FetchOptions = {}) {
  return apiFetch<number>(`/api/fines/user/${userId}/total-unpaid`, opts);
}

export function getFineById(id: number, opts: FetchOptions = {}) {
  return apiFetch<FineResponse>(`/api/fines/${id}`, opts);
}
