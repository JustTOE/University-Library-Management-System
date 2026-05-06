"use client";

import { useEffect, useRef, useState } from "react";
import { useActionState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { toast } from "sonner";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import {
  borrowAction,
  reserveAction,
} from "@/app/(app)/catalog/[id]/actions";
import { initialActionState } from "@/app/(app)/catalog/[id]/state";
import type { ActionState } from "@/app/(app)/catalog/[id]/state";
import type { BookResponse } from "@/lib/api/books";

export function BookActions({ book }: { book: BookResponse }) {
  const available = book.availableCopies ?? 0;
  if (available > 0) {
    return <BorrowAction book={book} />;
  }
  return <ReserveAction book={book} />;
}

/** Closes the dialog and surfaces success/error via Sonner whenever state changes. */
function useToastState(
  state: ActionState,
  closeDialog: () => void,
) {
  const router = useRouter();
  const last = useRef<ActionState>(initialActionState);
  useEffect(() => {
    if (state === last.current) return;
    last.current = state;
    if (state.status === "success") {
      toast.success(state.message);
      closeDialog();
      router.refresh();
    } else if (state.status === "error") {
      const isUnpaidFines = state.message.toLowerCase().includes("unpaid fine");
      if (isUnpaidFines) {
        toast.error(state.message, {
          action: {
            label: "Pay now",
            onClick: () => router.push("/my/fines"),
          },
        });
      } else {
        toast.error(state.message);
      }
      closeDialog();
    }
  }, [state, closeDialog, router]);
}

function BorrowAction({ book }: { book: BookResponse }) {
  const [open, setOpen] = useState(false);
  const [state, dispatch, pending] = useActionState(
    borrowAction,
    initialActionState,
  );
  useToastState(state, () => setOpen(false));

  return (
    <div className="flex flex-wrap items-center gap-2">
      <AlertDialog open={open} onOpenChange={setOpen}>
        <AlertDialogTrigger
          render={
            <Button data-testid="borrow-button" disabled={pending} size="lg">
              {pending ? "Working…" : "Borrow"}
            </Button>
          }
        />
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Borrow this book?</AlertDialogTitle>
            <AlertDialogDescription>
              Loans are 14 days. You can renew up to 3 times if no fines are
              outstanding.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <form action={dispatch}>
            <input type="hidden" name="bookId" value={book.id ?? ""} />
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction type="submit" disabled={pending}>
                Confirm borrow
              </AlertDialogAction>
            </AlertDialogFooter>
          </form>
        </AlertDialogContent>
      </AlertDialog>
      <Link
        href="/my/loans"
        className="text-sm text-muted-foreground hover:underline"
      >
        See my loans →
      </Link>
    </div>
  );
}

function ReserveAction({ book }: { book: BookResponse }) {
  const [open, setOpen] = useState(false);
  const [state, dispatch, pending] = useActionState(
    reserveAction,
    initialActionState,
  );
  useToastState(state, () => setOpen(false));

  return (
    <div className="flex flex-wrap items-center gap-2">
      <AlertDialog open={open} onOpenChange={setOpen}>
        <AlertDialogTrigger
          render={
            <Button
              data-testid="reserve-button"
              disabled={pending}
              size="lg"
              variant="secondary"
            >
              {pending ? "Working…" : "Reserve"}
            </Button>
          }
        />
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Reserve this book?</AlertDialogTitle>
            <AlertDialogDescription>
              You will be added to the wait list. The reservation expires 2 days
              after the book becomes available.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <form action={dispatch}>
            <input type="hidden" name="bookId" value={book.id ?? ""} />
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction type="submit" disabled={pending}>
                Confirm reservation
              </AlertDialogAction>
            </AlertDialogFooter>
          </form>
        </AlertDialogContent>
      </AlertDialog>
      <Link
        href="/my/reservations"
        className="text-sm text-muted-foreground hover:underline"
      >
        See my reservations →
      </Link>
    </div>
  );
}
