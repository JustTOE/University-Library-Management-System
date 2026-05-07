import { getTranslations } from "next-intl/server";

import { LoanStatusBadge } from "@/components/common/status-pill";
import { EmptyState } from "@/components/common/empty-state";
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { components } from "@/lib/api/types";
import { formatDateOnly } from "@/lib/format";

type LoanResponse = components["schemas"]["LoanResponse"];

export async function UserHistoryLoans({ loans }: { loans: LoanResponse[] }) {
  const t = await getTranslations("admin.users.history");
  if (loans.length === 0) {
    return (
      <EmptyState
        title={t("loansEmptyTitle")}
        description={t("loansEmptyDescription")}
      />
    );
  }
  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableCaption className="sr-only">{t("loanBook")}</TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead>{t("loanBook")}</TableHead>
            <TableHead className="hidden sm:table-cell">{t("loanBorrowed")}</TableHead>
            <TableHead className="hidden md:table-cell">{t("loanDue")}</TableHead>
            <TableHead>{t("loanStatus")}</TableHead>
            <TableHead className="hidden md:table-cell text-right">
              {t("loanRenewals")}
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
