import "server-only";

import type { components } from "./types";

export type NotificationResponse = components["schemas"]["NotificationResponse"];
export type UnreadCountResponse = components["schemas"]["UnreadCountResponse"];

// Phase 5+ will add list/markAcknowledged/unreadCount here.
