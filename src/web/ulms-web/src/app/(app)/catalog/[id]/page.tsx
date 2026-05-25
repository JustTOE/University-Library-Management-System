import Link from "next/link";
import { notFound } from "next/navigation";

import { AvailabilityBadge } from "@/components/catalog/availability-badge";
import { BookActions } from "@/components/catalog/book-actions";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { ApiError } from "@/lib/api/client";
import { getBookById } from "@/lib/api/books";
import { readSession } from "@/lib/auth/session";

export default async function BookDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const numericId = Number(id);
  if (!Number.isInteger(numericId) || numericId <= 0) {
    notFound();
  }

  const session = await readSession();
  const token = session?.token;

  let book;
  try {
    book = await getBookById(numericId, { token });
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  const available = book.availableCopies ?? 0;
  const total = book.totalCopies ?? 0;
  const isStudent = session?.user.role === "STUDENT";

  return (
    <div className="flex flex-col gap-6">
      <Link
        href="/catalog"
        className="text-sm text-muted-foreground hover:underline"
      >
        ← Back to catalog
      </Link>

      <Card>
        <CardHeader>
          <div className="flex items-start justify-between gap-4">
            <div className="flex flex-col gap-1">
              <CardTitle className="text-2xl">{book.title}</CardTitle>
              {book.author ? (
                <CardDescription>by {book.author}</CardDescription>
              ) : null}
            </div>
            <AvailabilityBadge
              available={available}
              total={total}
              variant="verbose"
            />
          </div>
        </CardHeader>
        <CardContent className="flex flex-col gap-6">
          <dl className="grid gap-3 text-sm sm:grid-cols-2">
            <Field label="ISBN" value={book.isbn} />
            <Field label="Subject" value={book.subject} />
            <Field
              label="Publication year"
              value={book.publicationYear?.toString()}
            />
            <Field label="Shelf" value={book.shelfNumber} />
            <Field
              label="Total copies"
              value={total ? String(total) : undefined}
            />
            <Field
              label="Available"
              value={String(available)}
            />
          </dl>

          <Separator />

          {isStudent ? (
            <BookActions book={book} />
          ) : (
            <p className="text-sm text-muted-foreground">
              Sign in as a student to borrow or reserve this book.
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function Field({ label, value }: { label: string; value?: string }) {
  return (
    <div className="flex flex-col gap-0.5">
      <dt className="text-xs uppercase tracking-wide text-muted-foreground">
        {label}
      </dt>
      <dd className="text-foreground">{value && value.length ? value : "—"}</dd>
    </div>
  );
}
