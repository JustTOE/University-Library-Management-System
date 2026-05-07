import { LoanStatusBadge } from "@/components/common/status-pill";
import { EmptyState } from "@/components/common/empty-state";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { components } from "@/lib/api/types";
import { formatDateOnly } from "@/lib/format";

type LoanResponse = components["schemas"]["LoanResponse"];

export function UserHistoryLoans({ loans }: { loans: LoanResponse[] }) {
  if (loans.length === 0) {
    return (
      <EmptyState
        title="No loans yet"
        description="This user has no past or current loans on record."
      />
    );
  }
  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Book</TableHead>
            <TableHead className="hidden sm:table-cell">Borrowed</TableHead>
            <TableHead className="hidden md:table-cell">Due</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="hidden md:table-cell text-right">
              Renewals
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loans.map((loan) => (
            <TableRow key={loan.id}>
              <TableCell className="font-medium">
                {loan.bookTitle ?? `Book #${loan.bookId ?? "—"}`}
              </TableCell>
              <TableCell className="hidden sm:table-cell text-muted-foreground">
                {formatDateOnly(loan.borrowDate)}
              </TableCell>
              <TableCell className="hidden md:table-cell text-muted-foreground">
                {formatDateOnly(loan.dueDate)}
              </TableCell>
              <TableCell>
                <LoanStatusBadge status={loan.status} />
              </TableCell>
              <TableCell className="hidden md:table-cell text-right">
                {loan.renewalCount ?? 0}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
