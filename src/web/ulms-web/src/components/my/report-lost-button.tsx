"use client";

import { useState } from "react";
import { useActionState } from "react";

import { reportLostAction } from "@/app/(app)/my/loans/actions";
import { initialActionState } from "@/app/(app)/my/loans/state";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

export function ReportLostButton({
  loanId,
  bookTitle,
}: {
  loanId: number;
  bookTitle: string;
}) {
  const [open, setOpen] = useState(false);
  const [state, dispatch, pending] = useActionState(
    reportLostAction,
    initialActionState,
  );
  useToastEffect(state, { onSuccess: () => setOpen(false) });

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger
        render={
          <Button
            size="sm"
            variant="destructive"
            disabled={pending}
            data-testid={`report-lost-${loanId}`}
          >
            Report lost
          </Button>
        }
      />
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Report book as lost</DialogTitle>
          <DialogDescription>
            Reporting &ldquo;{bookTitle}&rdquo; as lost adds a €50 replacement
            fee to your account (plus €1/day if the loan is overdue). The exact
            amount appears under your fines. This cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <form action={dispatch} className="flex flex-col gap-3">
          <input type="hidden" name="loanId" value={loanId} />
          <DialogFooter>
            <DialogClose
              render={
                <Button variant="outline" type="button" disabled={pending}>
                  Cancel
                </Button>
              }
            />
            <Button type="submit" variant="destructive" disabled={pending}>
              {pending ? "Reporting…" : "Report lost"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
