import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";

export type PaymentRequest = components["schemas"]["PaymentRequest"];
export type PaymentResponse = components["schemas"]["PaymentResponse"];

export function processPayment(body: PaymentRequest, opts: FetchOptions = {}) {
  return apiFetch<PaymentResponse>("/api/payments", {
    ...opts,
    method: "POST",
    body,
  });
}

export function listPaymentsByUser(userId: number, opts: FetchOptions = {}) {
  return apiFetch<PaymentResponse[]>(`/api/payments/user/${userId}`, opts);
}
