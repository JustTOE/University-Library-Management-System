import Link from "next/link";
import { Search } from "lucide-react";

import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export function CatalogSearchForm({
  defaults,
  action = "/catalog",
}: {
  defaults: { title?: string; author?: string; subject?: string };
  action?: string;
}) {
  const hasFilters =
    !!(defaults.title || defaults.author || defaults.subject);
  return (
    <form
      method="GET"
      action={action}
      className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4"
      role="search"
      aria-label="Search the catalog"
    >
      <div className="grid gap-3 sm:grid-cols-3">
        <div className="flex flex-col gap-1">
          <Label htmlFor="catalog-title">Title</Label>
          <Input
            id="catalog-title"
            name="title"
            defaultValue={defaults.title ?? ""}
            placeholder="e.g. Effective Java"
          />
        </div>
        <div className="flex flex-col gap-1">
          <Label htmlFor="catalog-author">Author</Label>
          <Input
            id="catalog-author"
            name="author"
            defaultValue={defaults.author ?? ""}
            placeholder="e.g. Bloch"
          />
        </div>
        <div className="flex flex-col gap-1">
          <Label htmlFor="catalog-subject">Subject</Label>
          <Input
            id="catalog-subject"
            name="subject"
            defaultValue={defaults.subject ?? ""}
            placeholder="e.g. Programming"
          />
        </div>
      </div>
      <div className="flex items-center gap-2">
        <Button type="submit">
          <Search className="size-4" aria-hidden /> Search
        </Button>
        {hasFilters ? (
          <Link
            href={action}
            className={buttonVariants({ variant: "ghost" })}
          >
            Clear
          </Link>
        ) : null}
      </div>
    </form>
  );
}
