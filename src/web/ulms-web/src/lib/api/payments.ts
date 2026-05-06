import "server-only";

import type { components } from "./types";

export type PaymentRequest = components["schemas"]["PaymentRequest"];
export type PaymentResponse = components["schemas"]["PaymentResponse"];

// Phase 5+ will add processPayment/listByUser here.
