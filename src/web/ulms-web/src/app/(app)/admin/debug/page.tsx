import { MakeOverdueButton } from "@/components/admin/make-overdue-button";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorAlert } from "@/components/common/error-alert";
import { LoanStatusBadge } from "@/components/common/status-pill";
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ApiError } from "@/lib/api/client";
import { listAllLoans, type LoanResponse } from "@/lib/api/loans";
import { readSession } from "@/lib/auth/session";
import { formatDateOnly } from "@/lib/format";

// A returned/lost loan can't be made overdue, so only these get the action.
function isActionable(status: LoanResponse["status"]): boolean {
  return status !== "RETURNED" && status !== "LOST";
}

export default async function AdminDebugPage() {
  const session = await readSession();
  const token = session?.token;

  let loans: LoanResponse[] = [];
  let errorMessage: string | null = null;

  try {
    loans = await listAllLoans({ token });
  } catch (error) {
    errorMessage =
      error instanceof ApiError
        ? error.message || "Could not load loans."
        : "Could not load loans.";
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <h2 className="text-lg font-semibold tracking-tight">
          Debug: overdue &amp; fines
        </h2>
        <p className="text-sm text-muted-foreground">
          Backdate any active loan to make it overdue and immediately create its
          unpaid fine, so you can exercise the pay-fine flow without waiting for
          the nightly job. Dev only — these endpoints do not exist in production.
        </p>
      </div>

      {errorMessage ? (
        <ErrorAlert message={errorMessage} />
      ) : loans.length === 0 ? (
        <EmptyState
          title="No loans"
          description="Borrow a book first, then return here to make it overdue."
        />
      ) : (
        <div className="rounded-lg border border-border bg-card">
          <Table>
            <TableCaption className="sr-only">All loans</TableCaption>
            <TableHeader>
              <TableRow>
                <TableHead>Loan</TableHead>
                <TableHead>Book</TableHead>
                <TableHead>Borrower</TableHead>
                <TableHead>Due</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loans.map((loan) => (
                <TableRow key={loan.id}>
                  <TableCell className="font-medium">#{loan.id}</TableCell>
                  <TableCell>{loan.bookTitle ?? "—"}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {loan.userName ?? `User ${loan.userId ?? "—"}`}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {formatDateOnly(loan.dueDate)}
                  </TableCell>
                  <TableCell>
                    <LoanStatusBadge status={loan.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    {loan.id && isActionable(loan.status) ? (
                      <MakeOverdueButton
                        loanId={loan.id}
                        bookTitle={loan.bookTitle ?? "this book"}
                      />
                    ) : (
                      <span className="text-xs text-muted-foreground">—</span>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}
