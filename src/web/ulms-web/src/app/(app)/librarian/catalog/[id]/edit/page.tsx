import Link from "next/link";
import { notFound } from "next/navigation";

import { BookForm } from "@/components/librarian/book-form";
import { buttonVariants } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { getBookById } from "@/lib/api/books";
import { ApiError } from "@/lib/api/client";
import { readSession } from "@/lib/auth/session";

export default async function EditBookPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const bookId = Number(id);
  if (!Number.isFinite(bookId) || bookId <= 0) notFound();

  const session = await readSession();
  const token = session?.token;

  let book;
  try {
    book = await getBookById(bookId, { token });
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between gap-2">
          <div className="flex flex-col gap-1">
            <CardTitle>Edit book</CardTitle>
            <CardDescription>
              Update catalog details for &quot;{book.title}&quot;.
            </CardDescription>
          </div>
          <Link
            href="/librarian/catalog"
            className={buttonVariants({ variant: "ghost", size: "sm" })}
          >
            Back to catalog
          </Link>
        </div>
      </CardHeader>
      <CardContent>
        <BookForm mode="edit" bookId={bookId} initial={book} />
      </CardContent>
    </Card>
  );
}
