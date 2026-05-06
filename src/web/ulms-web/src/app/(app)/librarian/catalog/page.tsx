import Link from "next/link";

import { CatalogPagination } from "@/components/catalog/catalog-pagination";
import { CatalogSearchForm } from "@/components/catalog/catalog-search-form";
import { LibrarianCatalogTable } from "@/components/librarian/librarian-catalog-table";
import { ErrorAlert } from "@/components/common/error-alert";
import { buttonVariants } from "@/components/ui/button";
import {
  listBooks,
  searchBooks,
  type BookResponse,
  type PageBookResponse,
} from "@/lib/api/books";
import { ApiError } from "@/lib/api/client";
import { readSession } from "@/lib/auth/session";

const DEFAULT_SIZE = 20;
const DEFAULT_SORT = "title,asc";

function pickString(
  v: string | string[] | undefined,
): string | undefined {
  if (Array.isArray(v)) return v[0];
  return v;
}

function pickInt(
  v: string | string[] | undefined,
  fallback: number,
): number {
  const value = pickString(v);
  if (!value) return fallback;
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
}

export default async function LibrarianCatalogPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const title = pickString(params.title);
  const author = pickString(params.author);
  const subject = pickString(params.subject);
  const page = pickInt(params.page, 0);
  const size = pickInt(params.size, DEFAULT_SIZE);
  const sort = pickString(params.sort) ?? DEFAULT_SORT;

  const filters = { title, author, subject };
  const hasFilters = !!(title || author || subject);

  const session = await readSession();
  const token = session?.token;

  let pageData: PageBookResponse | null = null;
  let errorMessage: string | null = null;

  try {
    pageData = hasFilters
      ? await searchBooks(filters, { page, size, sort }, { token })
      : await listBooks({ page, size, sort }, { token });
  } catch (error) {
    errorMessage =
      error instanceof ApiError
        ? error.message || "Could not load the catalog."
        : "Could not load the catalog.";
  }

  const books: BookResponse[] = pageData?.content ?? [];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between gap-2">
        <div className="flex flex-col gap-1">
          <h2 className="text-xl font-semibold tracking-tight">Catalog</h2>
          <p className="text-sm text-muted-foreground">
            Add, edit, or remove books in the library catalog.
          </p>
        </div>
        <Link
          href="/librarian/catalog/new"
          className={buttonVariants()}
          data-testid="new-book"
        >
          New book
        </Link>
      </div>

      <CatalogSearchForm
        defaults={{ title, author, subject }}
        action="/librarian/catalog"
      />

      {errorMessage ? (
        <ErrorAlert message={errorMessage} />
      ) : (
        <>
          <LibrarianCatalogTable books={books} />
          <CatalogPagination
            number={pageData?.number ?? 0}
            totalPages={pageData?.totalPages ?? 0}
            totalElements={pageData?.totalElements ?? 0}
            baseQuery={{
              title,
              author,
              subject,
              size: size === DEFAULT_SIZE ? undefined : String(size),
              sort: sort === DEFAULT_SORT ? undefined : sort,
            }}
          />
        </>
      )}
    </div>
  );
}
