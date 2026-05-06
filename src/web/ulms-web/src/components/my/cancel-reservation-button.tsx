"use client";

import { useState } from "react";
import { useActionState } from "react";

import { cancelReservationAction } from "@/app/(app)/my/reservations/actions";
import { initialActionState } from "@/app/(app)/my/reservations/state";
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
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

export function CancelReservationButton({
  reservationId,
  bookTitle,
}: {
  reservationId: number;
  bookTitle?: string;
}) {
  const [open, setOpen] = useState(false);
  const [state, dispatch, pending] = useActionState(
    cancelReservationAction,
    initialActionState,
  );
  const close = () => setOpen(false);
  useToastEffect(state, { onSuccess: close, onError: close });

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      <AlertDialogTrigger
        render={
          <Button
            size="sm"
            variant="outline"
            disabled={pending}
            data-testid={`cancel-reservation-${reservationId}`}
          >
            Cancel
          </Button>
        }
      />
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Cancel reservation?</AlertDialogTitle>
          <AlertDialogDescription>
            {bookTitle
              ? `You will lose your queue position for "${bookTitle}". This cannot be undone.`
              : "You will lose your queue position. This cannot be undone."}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <form action={dispatch}>
          <input type="hidden" name="reservationId" value={reservationId} />
          <AlertDialogFooter>
            <AlertDialogCancel>Keep reservation</AlertDialogCancel>
            <AlertDialogAction
              type="submit"
              variant="destructive"
              disabled={pending}
            >
              Cancel reservation
            </AlertDialogAction>
          </AlertDialogFooter>
        </form>
      </AlertDialogContent>
    </AlertDialog>
  );
}
