"use client";

import { useActionState, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

import { createBookAction } from "@/app/(app)/librarian/catalog/new/actions";
import { updateBookAction } from "@/app/(app)/librarian/catalog/[id]/edit/actions";
import { initialActionState, type ActionState } from "@/app/(app)/librarian/catalog/state";
import { Button, buttonVariants } from "@/components/ui/button";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import type { BookResponse, CreateBookRequest } from "@/lib/api/books";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

type BookFormProps =
  | { mode: "create"; initial?: undefined; bookId?: undefined }
  | { mode: "edit"; initial: BookResponse; bookId: number };

const NEXT_YEAR = new Date().getFullYear() + 1;

function defaultsFrom(initial: BookResponse | undefined): Partial<CreateBookRequest> {
  if (!initial) {
    return {
      totalCopies: 1,
      availableCopies: 1,
    };
  }
  return {
    title: initial.title ?? "",
    author: initial.author ?? "",
    isbn: initial.isbn ?? "",
    publicationYear: initial.publicationYear ?? undefined,
    subject: initial.subject ?? "",
    totalCopies: initial.totalCopies ?? 0,
    availableCopies: initial.availableCopies ?? 0,
    shelfNumber: initial.shelfNumber ?? "",
  };
}

export function BookForm(props: BookFormProps) {
  const router = useRouter();
  const action = props.mode === "create" ? createBookAction : updateBookAction;
  const [state, dispatch, pending] = useActionState<ActionState, FormData>(
    action,
    initialActionState,
  );

  const defaults = defaultsFrom(props.initial);

  const [totalCopies, setTotalCopies] = useState<number>(
    Number(defaults.totalCopies ?? 1),
  );
  const [availableCopies, setAvailableCopies] = useState<number>(
    Number(defaults.availableCopies ?? 1),
  );

  const fieldErrors = state.status === "error" ? state.fieldErrors ?? {} : {};
  const topError =
    state.status === "error" && Object.keys(fieldErrors).length === 0
      ? state.message
      : null;

  const copiesInvalid =
    Number.isFinite(totalCopies) &&
    Number.isFinite(availableCopies) &&
    availableCopies > totalCopies;

  useToastEffect(state, {
    onSuccess: () => router.push("/librarian/catalog"),
  });

  return (
    <form action={dispatch} className="flex flex-col gap-4" noValidate>
      {props.mode === "edit" ? (
        <input type="hidden" name="bookId" value={props.bookId} />
      ) : null}

      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="title">Title</FieldLabel>
          <Input
            id="title"
            name="title"
            required
            defaultValue={defaults.title ?? ""}
            aria-invalid={Boolean(fieldErrors.title) || undefined}
          />
          {fieldErrors.title && <FieldError>{fieldErrors.title}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="author">Author</FieldLabel>
          <Input
            id="author"
            name="author"
            required
            defaultValue={defaults.author ?? ""}
            aria-invalid={Boolean(fieldErrors.author) || undefined}
          />
          {fieldErrors.author && <FieldError>{fieldErrors.author}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="isbn">ISBN</FieldLabel>
          <Input
            id="isbn"
            name="isbn"
            required
            maxLength={20}
            defaultValue={defaults.isbn ?? ""}
            aria-invalid={Boolean(fieldErrors.isbn) || undefined}
          />
          <FieldDescription>Up to 20 characters; must be unique.</FieldDescription>
          {fieldErrors.isbn && <FieldError>{fieldErrors.isbn}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="publicationYear">
            Publication year{" "}
            <span className="text-muted-foreground">(optional)</span>
          </FieldLabel>
          <Input
            id="publicationYear"
            name="publicationYear"
            type="number"
            min={1000}
            max={NEXT_YEAR}
            defaultValue={defaults.publicationYear ?? ""}
            aria-invalid={Boolean(fieldErrors.publicationYear) || undefined}
          />
          {fieldErrors.publicationYear && (
            <FieldError>{fieldErrors.publicationYear}</FieldError>
          )}
        </Field>

        <Field>
          <FieldLabel htmlFor="subject">
            Subject <span className="text-muted-foreground">(optional)</span>
          </FieldLabel>
          <Input
            id="subject"
            name="subject"
            defaultValue={defaults.subject ?? ""}
            aria-invalid={Boolean(fieldErrors.subject) || undefined}
          />
          {fieldErrors.subject && (
            <FieldError>{fieldErrors.subject}</FieldError>
          )}
        </Field>

        <div className="grid gap-4 sm:grid-cols-2">
          <Field>
            <FieldLabel htmlFor="totalCopies">Total copies</FieldLabel>
            <Input
              id="totalCopies"
              name="totalCopies"
              type="number"
              min={0}
              required
              value={Number.isFinite(totalCopies) ? totalCopies : ""}
              onChange={(event) =>
                setTotalCopies(Number(event.currentTarget.value))
              }
              aria-invalid={Boolean(fieldErrors.totalCopies) || undefined}
            />
            {fieldErrors.totalCopies && (
              <FieldError>{fieldErrors.totalCopies}</FieldError>
            )}
          </Field>
          <Field>
            <FieldLabel htmlFor="availableCopies">Available copies</FieldLabel>
            <Input
              id="availableCopies"
              name="availableCopies"
              type="number"
              min={0}
              required
              value={Number.isFinite(availableCopies) ? availableCopies : ""}
              onChange={(event) =>
                setAvailableCopies(Number(event.currentTarget.value))
              }
              aria-invalid={
                Boolean(fieldErrors.availableCopies) || copiesInvalid || undefined
              }
            />
            {copiesInvalid ? (
              <FieldError>
                Available copies cannot exceed total copies.
              </FieldError>
            ) : (
              fieldErrors.availableCopies && (
                <FieldError>{fieldErrors.availableCopies}</FieldError>
              )
            )}
          </Field>
        </div>

        <Field>
          <FieldLabel htmlFor="shelfNumber">
            Shelf number{" "}
            <span className="text-muted-foreground">(optional)</span>
          </FieldLabel>
          <Input
            id="shelfNumber"
            name="shelfNumber"
            defaultValue={defaults.shelfNumber ?? ""}
            aria-invalid={Boolean(fieldErrors.shelfNumber) || undefined}
          />
          {fieldErrors.shelfNumber && (
            <FieldError>{fieldErrors.shelfNumber}</FieldError>
          )}
        </Field>
      </FieldGroup>

      {topError && (
        <FieldDescription className="text-destructive">
          {topError}
        </FieldDescription>
      )}

      <div className="flex items-center justify-end gap-2">
        <Link
          href="/librarian/catalog"
          className={buttonVariants({ variant: "ghost" })}
        >
          Cancel
        </Link>
        <Button type="submit" disabled={pending || copiesInvalid}>
          {pending
            ? "Saving…"
            : props.mode === "create"
              ? "Create book"
              : "Save changes"}
        </Button>
      </div>
    </form>
  );
}
