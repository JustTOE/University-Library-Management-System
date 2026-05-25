import { EmptyState } from "@/components/common/empty-state";
import { ErrorAlert } from "@/components/common/error-alert";
import { ReturnsList } from "@/components/librarian/returns-list";
import { ApiError } from "@/lib/api/client";
import { listAllLoans, type LoanResponse } from "@/lib/api/loans";
import { readSession } from "@/lib/auth/session";
import { daysUntil } from "@/lib/format";

// A loan is "outstanding" (book still physically out) until returned or lost.
const OUTSTANDING = new Set<LoanResponse["status"]>([
  "ACTIVE",
  "RENEWED",
  "OVERDUE",
]);

// Most-actionable first: soonest due date (and overdue) at the top.
function byDueDateAsc(a: LoanResponse, b: LoanResponse): number {
  const da = daysUntil(a.dueDate);
  const db = daysUntil(b.dueDate);
  if (da == null) return 1;
  if (db == null) return -1;
  return da - db;
}

export default async function LibrarianReturnsPage() {
  const session = await readSession();
  const token = session?.token;

  let loans: LoanResponse[] = [];
  let loadError: string | null = null;
  try {
    loans = await listAllLoans({ token });
  } catch (error) {
    loadError =
      error instanceof ApiError
        ? error.message || "Could not load loans."
        : "Could not load loans.";
  }

  const outstanding = loans
    .filter((loan) => OUTSTANDING.has(loan.status))
    .sort(byDueDateAsc);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-xl font-semibold tracking-tight">
          Outstanding loans
        </h2>
        <p className="text-sm text-muted-foreground">
          Every borrower&rsquo;s open loans. Confirm a return and the system
          puts the copy back, notifies any waiting reservation, and applies an
          overdue fine immediately if the loan is late.
        </p>
      </div>

      {loadError ? (
        <ErrorAlert title="Could not load loans" message={loadError} />
      ) : outstanding.length === 0 ? (
        <EmptyState
          title="No outstanding loans"
          description="Everything has been returned."
        />
      ) : (
        <ReturnsList loans={outstanding} />
      )}
    </div>
  );
}
