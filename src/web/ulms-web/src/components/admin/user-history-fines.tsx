import { FineStatusBadge } from "@/components/common/status-pill";
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
import { formatDateOnly, formatMoney } from "@/lib/format";

type FineResponse = components["schemas"]["FineResponse"];

export function UserHistoryFines({ fines }: { fines: FineResponse[] }) {
  if (fines.length === 0) {
    return (
      <EmptyState
        title="No fines on record"
        description="This user has not been charged any fines."
      />
    );
  }
  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Fine</TableHead>
            <TableHead className="hidden sm:table-cell">Calculated</TableHead>
            <TableHead className="text-right">Amount</TableHead>
            <TableHead>Status</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {fines.map((fine) => (
            <TableRow key={fine.id}>
              <TableCell className="font-medium">
                {fine.fineId ?? `Fine #${fine.id ?? "—"}`}
              </TableCell>
              <TableCell className="hidden sm:table-cell text-muted-foreground">
                {formatDateOnly(fine.calculatedDate)}
              </TableCell>
              <TableCell className="text-right">
                {formatMoney(fine.amount)}
              </TableCell>
              <TableCell>
                <FineStatusBadge status={fine.status} />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
