import Link from "next/link";

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
import type { BookResponse } from "@/lib/api/books";

import { DeleteBookButton } from "./delete-book-button";

export function LibrarianCatalogTable({ books }: { books: BookResponse[] }) {
  if (books.length === 0) {
    return (
      <EmptyState
        title="No books match your search"
        description="Try clearing filters or different keywords."
        action={
          <Link
            href="/librarian/catalog"
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
        <TableCaption className="sr-only">Library catalog</TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead>Title</TableHead>
            <TableHead>Author</TableHead>
            <TableHead className="hidden md:table-cell">Subject</TableHead>
            <TableHead className="hidden md:table-cell">ISBN</TableHead>
            <TableHead>Available</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {books.map((book) => (
            <TableRow key={book.id}>
              <TableCell className="font-medium">{book.title}</TableCell>
              <TableCell>{book.author ?? "—"}</TableCell>
              <TableCell className="hidden md:table-cell">
                {book.subject ?? "—"}
              </TableCell>
              <TableCell className="hidden md:table-cell text-muted-foreground">
                {book.isbn ?? "—"}
              </TableCell>
              <TableCell>
                <AvailabilityBadge
                  available={book.availableCopies ?? 0}
                  total={book.totalCopies ?? 0}
                />
              </TableCell>
              <TableCell className="text-right">
                <div className="flex justify-end gap-2">
                  <Link
                    href={`/librarian/catalog/${book.id}/edit`}
                    className={buttonVariants({ size: "sm", variant: "outline" })}
                  >
                    Edit
                  </Link>
                  <DeleteBookButton
                    bookId={book.id ?? 0}
                    title={book.title ?? "this book"}
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

function AvailabilityBadge({
  available,
  total,
}: {
  available: number;
  total: number;
}) {
  if (available <= 0) {
    return <Badge variant="outline">0 / {total}</Badge>;
  }
  return (
    <Badge variant={available < total ? "secondary" : "default"}>
      {available} / {total}
    </Badge>
  );
}
