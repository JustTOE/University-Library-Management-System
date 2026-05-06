import { Badge } from "@/components/ui/badge";
import type { FineResponse } from "@/lib/api/fines";
import type { LoanResponse } from "@/lib/api/loans";
import type { NotificationResponse } from "@/lib/api/notifications";
import type { ReservationResponse } from "@/lib/api/reservations";

type BadgeVariant = "default" | "secondary" | "destructive" | "outline";

function loanVariant(status: LoanResponse["status"]): BadgeVariant {
  switch (status) {
    case "ACTIVE":
    case "RENEWED":
      return "default";
    case "RETURNED":
      return "secondary";
    case "OVERDUE":
    case "LOST":
      return "destructive";
    default:
      return "outline";
  }
}

export function LoanStatusBadge({ status }: { status: LoanResponse["status"] }) {
  if (!status) return null;
  return <Badge variant={loanVariant(status)}>{status}</Badge>;
}

function reservationVariant(
  status: ReservationResponse["status"],
): BadgeVariant {
  switch (status) {
    case "ACTIVE":
      return "default";
    case "FULFILLED":
      return "secondary";
    case "CANCELLED":
    case "EXPIRED":
      return "outline";
    default:
      return "outline";
  }
}

export function ReservationStatusBadge({
  status,
}: {
  status: ReservationResponse["status"];
}) {
  if (!status) return null;
  return <Badge variant={reservationVariant(status)}>{status}</Badge>;
}

function fineVariant(status: FineResponse["status"]): BadgeVariant {
  switch (status) {
    case "UNPAID":
      return "destructive";
    case "PAID":
      return "secondary";
    case "WAIVED":
      return "outline";
    default:
      return "outline";
  }
}

export function FineStatusBadge({ status }: { status: FineResponse["status"] }) {
  if (!status) return null;
  return <Badge variant={fineVariant(status)}>{status}</Badge>;
}

function notificationVariant(
  status: NotificationResponse["status"],
): BadgeVariant {
  return status === "ACKNOWLEDGED" ? "secondary" : "default";
}

export function NotificationStatusBadge({
  status,
}: {
  status: NotificationResponse["status"];
}) {
  if (!status) return null;
  const label = status === "ACKNOWLEDGED" ? "Read" : "Unread";
  return <Badge variant={notificationVariant(status)}>{label}</Badge>;
}
