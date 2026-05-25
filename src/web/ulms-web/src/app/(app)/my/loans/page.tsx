import Link from "next/link";

import { ErrorAlert } from "@/components/common/error-alert";
import { EmptyState } from "@/components/common/empty-state";
import { LoanStatusBadge } from "@/components/common/status-pill";
import { RenewLoanForm } from "@/components/my/renew-loan-form";
import { ReportLostButton } from "@/components/my/report-lost-button";
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
import { listLoansByUser, type LoanResponse } from "@/lib/api/loans";
import { readSession } from "@/lib/auth/session";
import { daysUntil, formatDateOnly } from "@/lib/format";

const MAX_RENEWALS = 3;
const RENEWABLE_STATES = new Set<LoanResponse["status"]>(["ACTIVE", "RENEWED"]);
// A loan is still "open" (book physically out) until returned or written off.
const OPEN_STATES = new Set<LoanResponse["status"]>([
  "ACTIVE",
  "RENEWED",
  "OVERDUE",
]);

export default async function MyLoansPage() {
  const session = await readSession();
  if (!session?.user.id) {
    return <ErrorAlert message="Could not load your loans." />;
  }

  let loans: LoanResponse[] = [];
  let errorMessage: string | null = null;
  try {
    loans = await listLoansByUser(session.user.id, { token: session.token });
  } catch (error) {
    errorMessage =
      error instanceof ApiError
        ? error.message || "Could not load your loans."
        : "Could not load your loans.";
  }

  if (errorMessage) return <ErrorAlert message={errorMessage} />;

  if (loans.length === 0) {
    return (
      <EmptyState
        title="No loans yet"
        description="Borrow a book from the catalog to get started."
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
            <TableHead>Borrowed</TableHead>
            <TableHead>Due</TableHead>
            <TableHead className="hidden md:table-cell">Renewals</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">Action</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loans.map((loan) => (
            <LoanRow key={loan.id} loan={loan} />
          ))}
        </TableBody>
      </Table>
    </div>
  );
}

function LoanRow({ loan }: { loan: LoanResponse }) {
  const due = daysUntil(loan.dueDate);
  const renewals = loan.renewalCount ?? 0;
  const renewable =
    RENEWABLE_STATES.has(loan.status) && renewals < MAX_RENEWALS;
  const reportable = OPEN_STATES.has(loan.status) && Boolean(loan.id);

  return (
    <TableRow>
      <TableCell className="font-medium">
        {loan.bookId ? (
          <Link
            href={`/catalog/${loan.bookId}`}
            className="hover:underline"
          >
            {loan.bookTitle ?? `Book #${loan.bookId}`}
          </Link>
        ) : (
          <>{loan.bookTitle ?? "—"}</>
        )}
      </TableCell>
      <TableCell className="text-muted-foreground">
        {formatDateOnly(loan.borrowDate)}
      </TableCell>
      <TableCell>
        <div className="flex flex-col">
          <span>{formatDateOnly(loan.dueDate)}</span>
          {due != null && loan.status !== "RETURNED" ? (
            <span
              className={
                due < 0
                  ? "text-xs text-destructive"
                  : due <= 3
                    ? "text-xs text-foreground"
                    : "text-xs text-muted-foreground"
              }
            >
              {due < 0
                ? `${Math.abs(due)} days overdue`
                : due === 0
                  ? "due today"
                  : `${due} days left`}
            </span>
          ) : null}
        </div>
      </TableCell>
      <TableCell className="hidden md:table-cell">
        <Badge variant="outline">
          {renewals} / {MAX_RENEWALS}
        </Badge>
      </TableCell>
      <TableCell>
        <LoanStatusBadge status={loan.status} />
      </TableCell>
      <TableCell className="text-right">
        {renewable || reportable ? (
          <div className="flex items-center justify-end gap-2">
            {renewable && loan.id ? <RenewLoanForm loanId={loan.id} /> : null}
            {reportable && loan.id ? (
              <ReportLostButton
                loanId={loan.id}
                bookTitle={loan.bookTitle ?? `Book #${loan.bookId ?? loan.id}`}
              />
            ) : null}
          </div>
        ) : (
          <span className="text-xs text-muted-foreground">—</span>
        )}
      </TableCell>
    </TableRow>
  );
}
