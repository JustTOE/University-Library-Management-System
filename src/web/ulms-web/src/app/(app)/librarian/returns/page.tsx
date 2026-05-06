import { Search } from "lucide-react";

import { ConfirmReturnButton } from "@/components/librarian/confirm-return-button";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorAlert } from "@/components/common/error-alert";
import { LoanStatusBadge } from "@/components/common/status-pill";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ApiError } from "@/lib/api/client";
import { getLoanById, type LoanResponse } from "@/lib/api/loans";
import { readSession } from "@/lib/auth/session";
import { daysUntil, formatDateOnly } from "@/lib/format";

function pickString(v: string | string[] | undefined): string | undefined {
  if (Array.isArray(v)) return v[0];
  return v;
}

function isReturnedStatus(status: LoanResponse["status"]): boolean {
  return status === "RETURNED" || status === "LOST";
}

export default async function LibrarianReturnsPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const idRaw = pickString(params.id);
  const idNum = idRaw ? Number(idRaw) : NaN;
  const hasValidId = Boolean(idRaw) && Number.isFinite(idNum) && idNum > 0;

  const session = await readSession();
  const token = session?.token;

  let loan: LoanResponse | null = null;
  let lookupError: string | null = null;

  if (hasValidId) {
    try {
      loan = await getLoanById(idNum, { token });
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        lookupError = `No loan found with id ${idRaw}.`;
      } else {
        lookupError =
          error instanceof ApiError
            ? error.message || "Could not look up that loan."
            : "Could not look up that loan.";
      }
    }
  } else if (idRaw) {
    lookupError = "Enter a numeric loan id.";
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-xl font-semibold tracking-tight">Process return</h2>
        <p className="text-sm text-muted-foreground">
          Look up a loan by id, confirm the return, and the system will
          handle any fines and reservation fulfilment.
        </p>
      </div>

      <form
        method="GET"
        action="/librarian/returns"
        className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4"
        role="search"
        aria-label="Find a loan by id"
      >
        <div className="flex flex-col gap-1">
          <Label htmlFor="loan-id">Loan id</Label>
          <Input
            id="loan-id"
            name="id"
            inputMode="numeric"
            pattern="[0-9]+"
            defaultValue={idRaw ?? ""}
            placeholder="e.g. 42"
            data-testid="returns-search-id"
          />
        </div>
        <div>
          <Button type="submit" data-testid="returns-search-submit">
            <Search className="size-4" aria-hidden /> Find loan
          </Button>
        </div>
      </form>

      {lookupError ? (
        <ErrorAlert title="Loan lookup failed" message={lookupError} />
      ) : loan ? (
        <LoanCard loan={loan} />
      ) : (
        <EmptyState
          title="Search for a loan"
          description="Enter the loan id to look up borrower, book, and due date."
        />
      )}
    </div>
  );
}

function LoanCard({ loan }: { loan: LoanResponse }) {
  const returned = isReturnedStatus(loan.status);
  const days = daysUntil(loan.dueDate);
  const overdue = !returned && days != null && days < 0;

  return (
    <div className="flex flex-col gap-4">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between gap-2">
            <div className="flex flex-col gap-1">
              <CardTitle>{loan.bookTitle ?? `Loan #${loan.id}`}</CardTitle>
              <CardDescription>
                Borrower: {loan.userName ?? `user ${loan.userId ?? "?"}`}
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              {overdue ? <Badge variant="destructive">Overdue</Badge> : null}
              <LoanStatusBadge status={loan.status} />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <dl className="grid gap-3 text-sm sm:grid-cols-2">
            <div>
              <dt className="text-muted-foreground">Borrowed</dt>
              <dd>{formatDateOnly(loan.borrowDate)}</dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Due</dt>
              <dd>{formatDateOnly(loan.dueDate)}</dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Returned</dt>
              <dd>{loan.returnDate ? formatDateOnly(loan.returnDate) : "—"}</dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Renewals</dt>
              <dd>{loan.renewalCount ?? 0}</dd>
            </div>
          </dl>
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <ConfirmReturnButton
          loanId={loan.id ?? 0}
          alreadyReturned={returned}
        />
      </div>

      {returned ? (
        <Alert>
          <AlertTitle>Return processed</AlertTitle>
          <AlertDescription>
            If this loan was overdue, a fine will be calculated by the
            nightly job; if a reservation was waiting, that student has been
            notified.
          </AlertDescription>
        </Alert>
      ) : null}
    </div>
  );
}
