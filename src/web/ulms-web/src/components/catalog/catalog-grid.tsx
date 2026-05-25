import Link from "next/link";

import { AvailabilityBadge } from "@/components/catalog/availability-badge";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { EmptyState } from "@/components/common/empty-state";
import type { BookResponse } from "@/lib/api/books";

export function CatalogGrid({ books }: { books: BookResponse[] }) {
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
    <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {books.map((book) => (
        <li key={book.id} className="h-full">
          <Card className="h-full transition-shadow hover:shadow-md hover:ring-primary/30">
            <CardHeader>
              <CardTitle className="line-clamp-2 text-base">
                <Link
                  href={`/catalog/${book.id}`}
                  className="hover:text-primary hover:underline focus-visible:text-primary focus-visible:outline-none"
                >
                  {book.title}
                </Link>
              </CardTitle>
              <p className="text-sm text-muted-foreground">
                {book.author ? `by ${book.author}` : "Unknown author"}
              </p>
            </CardHeader>

            <CardContent className="flex flex-1 flex-col gap-3">
              <div className="flex flex-wrap items-center gap-1.5">
                {book.subject ? (
                  <Badge variant="secondary">{book.subject}</Badge>
                ) : null}
                {book.publicationYear ? (
                  <Badge variant="outline">{book.publicationYear}</Badge>
                ) : null}
              </div>
              <AvailabilityBadge
                available={book.availableCopies ?? 0}
                total={book.totalCopies ?? 0}
              />
            </CardContent>

            <CardFooter className="justify-end">
              <Link
                href={`/catalog/${book.id}`}
                className={buttonVariants({ size: "sm" })}
              >
                Open
              </Link>
            </CardFooter>
          </Card>
        </li>
      ))}
    </ul>
  );
}
