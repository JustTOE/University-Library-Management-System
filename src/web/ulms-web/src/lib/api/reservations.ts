import "server-only";

import type { components } from "./types";

export type ReservationRequest = components["schemas"]["ReservationRequest"];
export type ReservationResponse = components["schemas"]["ReservationResponse"];

// Phase 5+ will add create/cancel/listByUser here.
