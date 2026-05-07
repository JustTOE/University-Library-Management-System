import Link from "next/link";
import { Search } from "lucide-react";

import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const ROLES = [
  { value: "", label: "Any role" },
  { value: "STUDENT", label: "Student" },
  { value: "LIBRARIAN", label: "Librarian" },
  { value: "ADMIN", label: "Admin" },
] as const;

export function UserSearchForm({
  defaults,
  action = "/admin/users",
}: {
  defaults: { q?: string; role?: string };
  action?: string;
}) {
  const hasFilters = !!(defaults.q || defaults.role);
  return (
    <form
      method="GET"
      action={action}
      className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4"
      role="search"
      aria-label="Search users"
    >
      <div className="grid gap-3 sm:grid-cols-3">
        <div className="flex flex-col gap-1 sm:col-span-2">
          <Label htmlFor="user-q">Search</Label>
          <Input
            id="user-q"
            name="q"
            defaultValue={defaults.q ?? ""}
            placeholder="Name, email, university or staff ID"
          />
        </div>
        <div className="flex flex-col gap-1">
          <Label htmlFor="user-role">Role</Label>
          <select
            id="user-role"
            name="role"
            defaultValue={defaults.role ?? ""}
            className="h-9 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-1 focus-visible:outline-ring"
          >
            {ROLES.map((r) => (
              <option key={r.value} value={r.value}>
                {r.label}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div className="flex items-center gap-2">
        <Button type="submit">
          <Search className="size-4" aria-hidden /> Search
        </Button>
        {hasFilters ? (
          <Link href={action} className={buttonVariants({ variant: "ghost" })}>
            Clear
          </Link>
        ) : null}
      </div>
    </form>
  );
}
