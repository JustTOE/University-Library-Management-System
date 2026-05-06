import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";

export type CatalogPaginationProps = {
  number: number;
  totalPages: number;
  totalElements: number;
  baseQuery: Record<string, string | undefined>;
};

function buildHref(
  base: Record<string, string | undefined>,
  page: number,
): string {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(base)) {
    if (value !== undefined && value !== "" && key !== "page") {
      params.set(key, value);
    }
  }
  params.set("page", String(page));
  return `?${params.toString()}`;
}

function pageWindow(current: number, total: number): Array<number | "ellipsis"> {
  if (total <= 1) return [];
  const pages: Array<number | "ellipsis"> = [];
  const radius = 2;
  const start = Math.max(0, current - radius);
  const end = Math.min(total - 1, current + radius);

  if (start > 0) pages.push(0);
  if (start > 1) pages.push("ellipsis");
  for (let i = start; i <= end; i += 1) pages.push(i);
  if (end < total - 2) pages.push("ellipsis");
  if (end < total - 1) pages.push(total - 1);

  return pages;
}

export function CatalogPagination({
  number,
  totalPages,
  totalElements,
  baseQuery,
}: CatalogPaginationProps) {
  if (totalPages <= 1) {
    return (
      <p className="text-center text-xs text-muted-foreground">
        {totalElements} {totalElements === 1 ? "result" : "results"}
      </p>
    );
  }

  const window = pageWindow(number, totalPages);

  return (
    <div className="flex flex-col items-center gap-2">
      <Pagination>
        <PaginationContent>
          {number > 0 ? (
            <PaginationItem>
              <PaginationPrevious href={buildHref(baseQuery, number - 1)} />
            </PaginationItem>
          ) : null}
          {window.map((entry, idx) =>
            entry === "ellipsis" ? (
              <PaginationItem key={`ellipsis-${idx}`}>
                <PaginationEllipsis />
              </PaginationItem>
            ) : (
              <PaginationItem key={entry}>
                <PaginationLink
                  href={buildHref(baseQuery, entry)}
                  isActive={entry === number}
                >
                  {entry + 1}
                </PaginationLink>
              </PaginationItem>
            ),
          )}
          {number < totalPages - 1 ? (
            <PaginationItem>
              <PaginationNext href={buildHref(baseQuery, number + 1)} />
            </PaginationItem>
          ) : null}
        </PaginationContent>
      </Pagination>
      <p className="text-xs text-muted-foreground">
        Page {number + 1} of {totalPages} · {totalElements}{" "}
        {totalElements === 1 ? "result" : "results"}
      </p>
    </div>
  );
}
