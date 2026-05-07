"use client";

import { useState } from "react";
import { useActionState } from "react";

import { deleteUserAction } from "@/app/(app)/admin/users/actions";
import { initialActionState } from "@/app/(app)/admin/users/state";
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

export function DeleteUserButton({
  userId,
  name,
}: {
  userId: number;
  name: string;
}) {
  const [open, setOpen] = useState(false);
  const [state, dispatch, pending] = useActionState(
    deleteUserAction,
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
            variant="destructive"
            disabled={pending}
            data-testid={`delete-user-${userId}`}
          >
            Delete
          </Button>
        }
      />
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Delete {name}?</AlertDialogTitle>
          <AlertDialogDescription>
            This permanently deletes the user record and cannot be undone. Users
            with loans or fines on record cannot be deleted.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <form action={dispatch}>
          <input type="hidden" name="userId" value={userId} />
          <input type="hidden" name="name" value={name} />
          <AlertDialogFooter>
            <AlertDialogCancel>Keep user</AlertDialogCancel>
            <AlertDialogAction
              type="submit"
              variant="destructive"
              disabled={pending}
            >
              Delete user
            </AlertDialogAction>
          </AlertDialogFooter>
        </form>
      </AlertDialogContent>
    </AlertDialog>
  );
}
