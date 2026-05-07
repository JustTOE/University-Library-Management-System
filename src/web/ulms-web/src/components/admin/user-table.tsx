import Link from "next/link";
import { getTranslations } from "next-intl/server";

import { ActiveToggleButton } from "@/components/admin/active-toggle-button";
import { DeleteUserButton } from "@/components/admin/delete-user-button";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { EmptyState } from "@/components/common/empty-state";
import type { UserResponse } from "@/lib/api/users";

function RoleBadge({
  role,
  labels,
}: {
  role: UserResponse["role"];
  labels: { admin: string; librarian: string; student: string };
}) {
  switch (role) {
    case "ADMIN":
      return <Badge variant="default">{labels.admin}</Badge>;
    case "LIBRARIAN":
      return <Badge variant="secondary">{labels.librarian}</Badge>;
    case "STUDENT":
      return <Badge variant="outline">{labels.student}</Badge>;
    default:
      return <Badge variant="outline">—</Badge>;
  }
}

function ActiveBadge({
  isActive,
  labels,
}: {
  isActive: boolean;
  labels: { active: string; inactive: string };
}) {
  return isActive ? (
    <Badge variant="secondary" className="text-emerald-700 dark:text-emerald-400">
      {labels.active}
    </Badge>
  ) : (
    <Badge variant="outline" className="text-muted-foreground">
      {labels.inactive}
    </Badge>
  );
}

function identifierLabel(user: UserResponse): string {
  if (user.universityId) return `Univ. ${user.universityId}`;
  if (user.staffId) return `Staff ${user.staffId}`;
  return "—";
}

export async function UserTable({ users }: { users: UserResponse[] }) {
  const t = await getTranslations("admin.users");
  const tCommon = await getTranslations("common");

  const roleLabels = {
    admin: t("roleAdmin"),
    librarian: t("roleLibrarian"),
    student: t("roleStudent"),
  };
  const activeLabels = { active: t("active"), inactive: t("inactive") };

  if (users.length === 0) {
    return (
      <EmptyState
        title={t("emptyTitle")}
        description={t("emptyDescription")}
        action={
          <Link
            href="/admin/users"
            className={buttonVariants({ variant: "outline" })}
          >
            {t("clearFilters")}
          </Link>
        }
      />
    );
  }

  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableCaption className="sr-only">{t("title")}</TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead>{t("table.name")}</TableHead>
            <TableHead>{t("table.email")}</TableHead>
            <TableHead>{t("table.role")}</TableHead>
            <TableHead className="hidden md:table-cell">
              {t("table.identifier")}
            </TableHead>
            <TableHead>{t("table.status")}</TableHead>
            <TableHead className="text-right">{t("table.actions")}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {users.map((user) => (
            <TableRow key={user.id}>
              <TableCell className="font-medium">{user.name ?? "—"}</TableCell>
              <TableCell className="text-muted-foreground">
                {user.email ?? "—"}
              </TableCell>
              <TableCell>
                <RoleBadge role={user.role} labels={roleLabels} />
              </TableCell>
              <TableCell className="hidden md:table-cell text-muted-foreground">
                {identifierLabel(user)}
              </TableCell>
              <TableCell>
                <ActiveBadge isActive={Boolean(user.isActive)} labels={activeLabels} />
              </TableCell>
              <TableCell className="text-right">
                <div className="flex justify-end gap-2">
                  <Link
                    href={`/admin/users/${user.id}`}
                    className={buttonVariants({ size: "sm", variant: "ghost" })}
                  >
                    {tCommon("view")}
                  </Link>
                  <Link
                    href={`/admin/users/${user.id}/edit`}
                    className={buttonVariants({ size: "sm", variant: "outline" })}
                  >
                    {tCommon("edit")}
                  </Link>
                  <ActiveToggleButton
                    userId={user.id ?? 0}
                    name={user.name ?? "this user"}
                    isActive={Boolean(user.isActive)}
                  />
                  <DeleteUserButton
                    userId={user.id ?? 0}
                    name={user.name ?? "this user"}
                  />
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
