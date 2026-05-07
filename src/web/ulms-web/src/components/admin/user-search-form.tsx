"use client";

import Link from "next/link";
import { Search } from "lucide-react";
import { useTranslations } from "next-intl";

import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export function UserSearchForm({
  defaults,
  action = "/admin/users",
}: {
  defaults: { q?: string; role?: string };
  action?: string;
}) {
  const t = useTranslations("admin.users");
  const tCommon = useTranslations("common");
  const hasFilters = !!(defaults.q || defaults.role);
  const ROLES = [
    { value: "", label: t("anyRole") },
    { value: "STUDENT", label: t("roleStudent") },
    { value: "LIBRARIAN", label: t("roleLibrarian") },
    { value: "ADMIN", label: t("roleAdmin") },
  ];
  return (
    <form
      method="GET"
      action={action}
      className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4"
      role="search"
      aria-label={t("searchLabel")}
    >
      <div className="grid gap-3 sm:grid-cols-3">
        <div className="flex flex-col gap-1 sm:col-span-2">
          <Label htmlFor="user-q">{t("searchLabel")}</Label>
          <Input
            id="user-q"
            name="q"
            defaultValue={defaults.q ?? ""}
            placeholder={t("searchPlaceholder")}
          />
        </div>
        <div className="flex flex-col gap-1">
          <Label htmlFor="user-role">{t("roleLabel")}</Label>
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
          <Search className="size-4" aria-hidden /> {tCommon("search")}
        </Button>
        {hasFilters ? (
          <Link href={action} className={buttonVariants({ variant: "ghost" })}>
            {tCommon("clear")}
          </Link>
        ) : null}
      </div>
    </form>
  );
}
