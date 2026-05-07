import { getTranslations } from "next-intl/server";

import { FineStatusBadge } from "@/components/common/status-pill";
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
import { formatDateOnly, formatMoney } from "@/lib/format";

type FineResponse = components["schemas"]["FineResponse"];

export async function UserHistoryFines({ fines }: { fines: FineResponse[] }) {
  const t = await getTranslations("admin.users.history");
  if (fines.length === 0) {
    return (
      <EmptyState
        title={t("finesEmptyTitle")}
        description={t("finesEmptyDescription")}
      />
    );
  }
  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableCaption className="sr-only">{t("fineId")}</TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead>{t("fineId")}</TableHead>
            <TableHead className="hidden sm:table-cell">{t("fineCalculated")}</TableHead>
            <TableHead className="text-right">{t("fineAmount")}</TableHead>
            <TableHead>{t("fineStatus")}</TableHead>
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
