import "server-only";

import type { components } from "./types";

export type LoanResponse = components["schemas"]["LoanResponse"];
export type BorrowRequest = components["schemas"]["BorrowRequest"];
export type PageLoanResponse = components["schemas"]["PageLoanResponse"];

// Phase 5+ will add borrow/return/renew/listByUser here.
