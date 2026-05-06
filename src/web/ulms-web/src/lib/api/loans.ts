import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";
import type { PageQuery } from "./books";

export type LoanResponse = components["schemas"]["LoanResponse"];
export type BorrowRequest = components["schemas"]["BorrowRequest"];
export type PageLoanResponse = components["schemas"]["PageLoanResponse"];

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
