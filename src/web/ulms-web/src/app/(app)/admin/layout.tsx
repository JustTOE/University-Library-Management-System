import { headers } from "next/headers";
import Link from "next/link";
import { getTranslations } from "next-intl/server";

import { requireRole } from "@/lib/auth/guards";

export default async function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const hdrs = await headers();
  const path = hdrs.get("x-current-path") ?? "/admin/users";
  await requireRole(path, "ADMIN");
  const t = await getTranslations("admin");
  const tNav = await getTranslations("navbar.adminItems");

  const NAV = [{ href: "/admin/users", label: tNav("users") }];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold tracking-tight">{t("title")}</h1>
        <p className="text-sm text-muted-foreground">{t("subtitle")}</p>
      </div>
      <nav
        aria-label={t("title")}
        className="flex flex-wrap gap-1 rounded-lg border border-border bg-card p-1 text-sm"
      >
        {NAV.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className="rounded-md px-3 py-1.5 text-muted-foreground hover:bg-muted hover:text-foreground aria-[current=page]:bg-muted aria-[current=page]:text-foreground"
          >
            {item.label}
          </Link>
        ))}
      </nav>
      {children}
    </div>
  );
}
