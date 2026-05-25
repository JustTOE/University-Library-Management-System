import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";
import type { PageQuery } from "./books";
import type { FineResponse } from "./fines";

export type LoanResponse = components["schemas"]["LoanResponse"];
export type BorrowRequest = components["schemas"]["BorrowRequest"];
export type PageLoanResponse = components["schemas"]["PageLoanResponse"];

export function listAllLoans(opts: FetchOptions = {}) {
  return apiFetch<LoanResponse[]>("/api/loans", opts);
}

/**
 * Dev-only: backdate a loan's due date by `days` and create its overdue fine
 * immediately. Backed by /api/debug, which only exists outside the prod
 * profile, so this 404s in production.
 */
export function makeLoanOverdue(
  loanId: number,
  days: number,
  opts: FetchOptions = {},
) {
  return apiFetch<FineResponse>(`/api/debug/loans/${loanId}/make-overdue`, {
    ...opts,
    method: "POST",
    query: { days },
  });
}

export function borrow(body: BorrowRequest, opts: FetchOptions = {}) {
  return apiFetch<LoanResponse>("/api/loans/borrow", {
    ...opts,
    method: "POST",
    body,
  });
}

export function renew(loanId: number, opts: FetchOptions = {}) {
  return apiFetch<LoanResponse>(`/api/loans/${loanId}/renew`, {
    ...opts,
    method: "PUT",
  });
}

export function listLoansByUser(userId: number, opts: FetchOptions = {}) {
  return apiFetch<LoanResponse[]>(`/api/loans/user/${userId}`, opts);
}

export function listLoansByUserPaged(
  userId: number,
  q: PageQuery = {},
  opts: FetchOptions = {},
) {
  return apiFetch<PageLoanResponse>(`/api/loans/user/${userId}/paged`, {
    ...opts,
    query: { page: q.page, size: q.size, sort: q.sort },
  });
}

export function getLoanById(id: number, opts: FetchOptions = {}) {
  return apiFetch<LoanResponse>(`/api/loans/${id}`, opts);
}

export function returnLoan(id: number, opts: FetchOptions = {}) {
  return apiFetch<LoanResponse>(`/api/loans/${id}/return`, {
    ...opts,
    method: "PUT",
  });
}

export function reportLost(id: number, opts: FetchOptions = {}) {
  return apiFetch<LoanResponse>(`/api/loans/${id}/report-lost`, {
    ...opts,
    method: "PUT",
  });
}
