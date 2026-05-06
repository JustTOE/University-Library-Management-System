import "server-only";

import type { components } from "./types";

export type BookResponse = components["schemas"]["BookResponse"];
export type CreateBookRequest = components["schemas"]["CreateBookRequest"];
export type PageBookResponse = components["schemas"]["PageBookResponse"];

// Phase 5+ will add list/search/getById/create/update/delete here.
