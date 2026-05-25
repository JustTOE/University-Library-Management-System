"use client";

import { useState } from "react";
import { useActionState } from "react";

import { makeOverdueAction } from "@/app/(app)/admin/debug/actions";
import { initialActionState } from "@/app/(app)/admin/debug/state";
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
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

export function MakeOverdueButton({
  loanId,
  bookTitle,
}: {
  loanId: number;
  bookTitle: string;
}) {
  const [open, setOpen] = useState(false);
  const [days, setDays] = useState("10");
  const [state, dispatch, pending] = useActionState(
    makeOverdueAction,
    initialActionState,
  );
  useToastEffect(state, { onSuccess: () => setOpen(false) });

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger
        render={
          <Button
            size="sm"
            variant="secondary"
            disabled={pending}
            data-testid={`make-overdue-${loanId}`}
          >
            Make overdue
          </Button>
        }
      />
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Make loan overdue</DialogTitle>
          <DialogDescription>
            Backdates &ldquo;{bookTitle}&rdquo; (loan #{loanId}) and creates an
            unpaid fine of €1.00/day right away. Dev only.
          </DialogDescription>
        </DialogHeader>
        <form action={dispatch} className="flex flex-col gap-3">
          <input type="hidden" name="loanId" value={loanId} />
          {/* Mirror the day count into a hidden input the action reads; the
              visible Base UI Input stays uncontrolled-friendly and out of the
              native submit/validation path (matches PayFineButton's pattern). */}
          <input type="hidden" name="days" value={days} />
          <div className="flex flex-col gap-1.5">
            <Label htmlFor={`days-${loanId}`}>Days overdue</Label>
            <Input
              id={`days-${loanId}`}
              type="number"
              min={1}
              value={days}
              onChange={(e) => setDays(e.target.value)}
            />
          </div>
          <DialogFooter>
            <DialogClose
              render={
                <Button variant="outline" type="button" disabled={pending}>
                  Cancel
                </Button>
              }
            />
            <Button type="submit" disabled={pending}>
              {pending ? "Working…" : "Make overdue"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
