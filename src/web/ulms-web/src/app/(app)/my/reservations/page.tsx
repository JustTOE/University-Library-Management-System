import Link from "next/link";

import { ErrorAlert } from "@/components/common/error-alert";
import { EmptyState } from "@/components/common/empty-state";
import { ReservationStatusBadge } from "@/components/common/status-pill";
import { CancelReservationButton } from "@/components/my/cancel-reservation-button";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ApiError } from "@/lib/api/client";
import {
  listReservationsByUser,
  type ReservationResponse,
} from "@/lib/api/reservations";
import { readSession } from "@/lib/auth/session";
import { formatDateOnly, formatDateTime } from "@/lib/format";

export default async function MyReservationsPage() {
  const session = await readSession();
  if (!session?.user.id) {
    return <ErrorAlert message="Could not load your reservations." />;
  }

  let reservations: ReservationResponse[] = [];
  let errorMessage: string | null = null;
  try {
    reservations = await listReservationsByUser(session.user.id, {
      token: session.token,
    });
  } catch (error) {
    errorMessage =
      error instanceof ApiError
        ? error.message || "Could not load your reservations."
        : "Could not load your reservations.";
  }

  if (errorMessage) return <ErrorAlert message={errorMessage} />;

  if (reservations.length === 0) {
    return (
      <EmptyState
        title="No reservations"
        description="When a book has no copies, you can reserve it from the catalog."
        action={
          <Link href="/catalog" className={buttonVariants()}>
            Browse catalog
          </Link>
        }
      />
    );
  }

  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Book</TableHead>
            <TableHead>Reserved at</TableHead>
            <TableHead>Expiry</TableHead>
            <TableHead className="hidden md:table-cell">Queue</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">Action</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {reservations.map((reservation) => (
            <ReservationRow
              key={reservation.id}
              reservation={reservation}
            />
          ))}
        </TableBody>
      </Table>
    </div>
  );
}

function ReservationRow({
  reservation,
}: {
  reservation: ReservationResponse;
}) {
  const isActive = reservation.status === "ACTIVE";
  return (
    <TableRow>
      <TableCell className="font-medium">
        {reservation.bookId ? (
          <Link
            href={`/catalog/${reservation.bookId}`}
            className="hover:underline"
          >
            {reservation.bookTitle ?? `Book #${reservation.bookId}`}
          </Link>
        ) : (
          <>{reservation.bookTitle ?? "—"}</>
        )}
      </TableCell>
      <TableCell className="text-muted-foreground">
        {formatDateTime(reservation.reservedAt)}
      </TableCell>
      <TableCell>{formatDateOnly(reservation.expiryDate)}</TableCell>
      <TableCell className="hidden md:table-cell">
        {reservation.queuePosition != null ? (
          <Badge variant="outline">#{reservation.queuePosition}</Badge>
        ) : (
          <span className="text-xs text-muted-foreground">—</span>
        )}
      </TableCell>
      <TableCell>
        <ReservationStatusBadge status={reservation.status} />
      </TableCell>
      <TableCell className="text-right">
        {isActive && reservation.id ? (
          <CancelReservationButton
            reservationId={reservation.id}
            bookTitle={reservation.bookTitle}
          />
        ) : (
          <span className="text-xs text-muted-foreground">—</span>
        )}
      </TableCell>
    </TableRow>
  );
}
