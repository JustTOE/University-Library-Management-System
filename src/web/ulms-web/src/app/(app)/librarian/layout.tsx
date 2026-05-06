import { headers } from "next/headers";
import Link from "next/link";

import { requireRole } from "@/lib/auth/guards";

const NAV = [
  { href: "/librarian/catalog", label: "Catalog" },
  { href: "/librarian/returns", label: "Returns" },
];

export default async function LibrarianLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const hdrs = await headers();
  const path = hdrs.get("x-current-path") ?? "/librarian/catalog";
  await requireRole(path, "LIBRARIAN", "ADMIN");

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold tracking-tight">Librarian</h1>
        <p className="text-sm text-muted-foreground">
          Manage the catalog and process returns.
        </p>
      </div>
      <nav
        aria-label="Librarian sections"
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
