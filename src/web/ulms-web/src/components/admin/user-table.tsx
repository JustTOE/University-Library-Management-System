import Link from "next/link";

import { ActiveToggleButton } from "@/components/admin/active-toggle-button";
import { DeleteUserButton } from "@/components/admin/delete-user-button";
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
import { EmptyState } from "@/components/common/empty-state";
import type { UserResponse } from "@/lib/api/users";

function RoleBadge({ role }: { role: UserResponse["role"] }) {
  switch (role) {
    case "ADMIN":
      return <Badge variant="default">Admin</Badge>;
    case "LIBRARIAN":
      return <Badge variant="secondary">Librarian</Badge>;
    case "STUDENT":
      return <Badge variant="outline">Student</Badge>;
    default:
      return <Badge variant="outline">—</Badge>;
  }
}

function ActiveBadge({ isActive }: { isActive: boolean }) {
  return isActive ? (
    <Badge variant="secondary" className="text-emerald-700 dark:text-emerald-400">
      Active
    </Badge>
  ) : (
    <Badge variant="outline" className="text-muted-foreground">
      Inactive
    </Badge>
  );
}

function identifierLabel(user: UserResponse): string {
  if (user.universityId) return `Univ. ${user.universityId}`;
  if (user.staffId) return `Staff ${user.staffId}`;
  return "—";
}

export function UserTable({ users }: { users: UserResponse[] }) {
  if (users.length === 0) {
    return (
      <EmptyState
        title="No users match your filters"
        description="Try a different search or clear filters."
        action={
          <Link
            href="/admin/users"
            className={buttonVariants({ variant: "outline" })}
          >
            Clear filters
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
            <TableHead>Name</TableHead>
            <TableHead>Email</TableHead>
            <TableHead>Role</TableHead>
            <TableHead className="hidden md:table-cell">Identifier</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">Actions</TableHead>
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
                <RoleBadge role={user.role} />
              </TableCell>
              <TableCell className="hidden md:table-cell text-muted-foreground">
                {identifierLabel(user)}
              </TableCell>
              <TableCell>
                <ActiveBadge isActive={Boolean(user.isActive)} />
              </TableCell>
              <TableCell className="text-right">
                <div className="flex justify-end gap-2">
                  <Link
                    href={`/admin/users/${user.id}`}
                    className={buttonVariants({ size: "sm", variant: "ghost" })}
                  >
                    View
                  </Link>
                  <Link
                    href={`/admin/users/${user.id}/edit`}
                    className={buttonVariants({ size: "sm", variant: "outline" })}
                  >
                    Edit
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
