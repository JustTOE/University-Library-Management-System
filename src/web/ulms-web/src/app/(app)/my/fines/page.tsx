import Link from "next/link";

import { ErrorAlert } from "@/components/common/error-alert";
import { EmptyState } from "@/components/common/empty-state";
import { FineStatusBadge } from "@/components/common/status-pill";
import { PayFineButton } from "@/components/my/pay-fine-button";
import { buttonVariants } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
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
  getTotalUnpaid,
  listFinesByUser,
  type FineResponse,
} from "@/lib/api/fines";
import { readSession } from "@/lib/auth/session";
import { formatDateOnly, formatMoney } from "@/lib/format";

export default async function MyFinesPage() {
  const session = await readSession();
  if (!session?.user.id) {
    return <ErrorAlert message="Could not load your fines." />;
  }

  const userId = session.user.id;
  const token = session.token;

  let fines: FineResponse[] = [];
  let total = 0;
  let errorMessage: string | null = null;

  try {
    [fines, total] = await Promise.all([
      listFinesByUser(userId, { token }),
      getTotalUnpaid(userId, { token }),
    ]);
  } catch (error) {
    errorMessage =
      error instanceof ApiError
        ? error.message || "Could not load your fines."
        : "Could not load your fines.";
  }

  if (errorMessage) return <ErrorAlert message={errorMessage} />;

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Outstanding balance</CardTitle>
          <CardDescription>
            Sum of every UNPAID fine on your account.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-3xl font-semibold tracking-tight">
            {formatMoney(total)}
          </p>
        </CardContent>
      </Card>

      {fines.length === 0 ? (
        <EmptyState
          title="No fines"
          description="Return books on time to keep it that way."
          action={
            <Link href="/my/loans" className={buttonVariants({ variant: "outline" })}>
              See my loans
            </Link>
          }
        />
      ) : (
        <div className="rounded-lg border border-border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Loan</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Calculated</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {fines.map((fine) => (
                <FineRow key={fine.id} fine={fine} />
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

function FineRow({ fine }: { fine: FineResponse }) {
  return (
    <TableRow>
      <TableCell className="font-medium">
        Loan #{fine.loanId ?? "—"}
      </TableCell>
      <TableCell>{formatMoney(fine.amount)}</TableCell>
      <TableCell className="text-muted-foreground">
        {formatDateOnly(fine.calculatedDate)}
      </TableCell>
      <TableCell>
        <FineStatusBadge status={fine.status} />
      </TableCell>
      <TableCell className="text-right">
        {fine.status === "UNPAID" && fine.id ? (
          <PayFineButton fine={fine} />
        ) : (
          <span className="text-xs text-muted-foreground">—</span>
        )}
      </TableCell>
    </TableRow>
  );
}
