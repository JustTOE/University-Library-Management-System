"use client";

import { useState } from "react";
import { useActionState } from "react";

import { setActiveAction } from "@/app/(app)/admin/users/actions";
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

export function ActiveToggleButton({
  userId,
  name,
  isActive,
}: {
  userId: number;
  name: string;
  isActive: boolean;
}) {
  const [open, setOpen] = useState(false);
  const [state, dispatch, pending] = useActionState(
    setActiveAction,
    initialActionState,
  );
  const close = () => setOpen(false);
  useToastEffect(state, { onSuccess: close, onError: close });

  const willActivate = !isActive;

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      <AlertDialogTrigger
        render={
          <Button
            size="sm"
            variant={willActivate ? "outline" : "secondary"}
            disabled={pending}
            data-testid={`toggle-active-${userId}`}
          >
            {willActivate ? "Reactivate" : "Deactivate"}
          </Button>
        }
      />
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            {willActivate ? "Reactivate" : "Deactivate"} {name}?
          </AlertDialogTitle>
          <AlertDialogDescription>
            {willActivate
              ? "The user will be able to sign in again."
              : "The user will be unable to sign in until reactivated."}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <form action={dispatch}>
          <input type="hidden" name="userId" value={userId} />
          <input
            type="hidden"
            name="nextActive"
            value={willActivate ? "true" : "false"}
          />
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction type="submit" disabled={pending}>
              {willActivate ? "Reactivate" : "Deactivate"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </form>
      </AlertDialogContent>
    </AlertDialog>
  );
}
