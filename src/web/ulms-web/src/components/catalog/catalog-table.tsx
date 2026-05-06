import Link from "next/link";

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
import type { BookResponse } from "@/lib/api/books";

export function CatalogTable({ books }: { books: BookResponse[] }) {
  if (books.length === 0) {
    return (
      <EmptyState
        title="No books match your search"
        description="Try clearing filters or different keywords."
        action={
          <Link
            href="/catalog"
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
            <TableHead>Title</TableHead>
            <TableHead>Author</TableHead>
            <TableHead className="hidden md:table-cell">Subject</TableHead>
            <TableHead className="hidden md:table-cell">ISBN</TableHead>
            <TableHead>Available</TableHead>
            <TableHead className="text-right">Action</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {books.map((book) => (
            <TableRow key={book.id}>
              <TableCell className="font-medium">
                <Link
                  href={`/catalog/${book.id}`}
                  className="hover:underline"
                >
                  {book.title}
                </Link>
              </TableCell>
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
                <Link
                  href={`/catalog/${book.id}`}
                  className={buttonVariants({ size: "sm", variant: "outline" })}
                >
                  Open
                </Link>
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
