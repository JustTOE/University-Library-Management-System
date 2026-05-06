"use client";

import { useActionState } from "react";

import { confirmReturnAction } from "@/app/(app)/librarian/returns/actions";
import { initialActionState } from "@/app/(app)/librarian/returns/state";
import { Button } from "@/components/ui/button";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

export function ConfirmReturnButton({
  loanId,
  alreadyReturned,
}: {
  loanId: number;
  alreadyReturned: boolean;
}) {
  const [state, dispatch, pending] = useActionState(
    confirmReturnAction,
    initialActionState,
  );
  useToastEffect(state);

  if (alreadyReturned) {
    return (
      <Button disabled variant="outline" data-testid="already-returned">
        Already returned
      </Button>
    );
  }

  return (
    <form action={dispatch}>
      <input type="hidden" name="loanId" value={loanId} />
      <Button type="submit" disabled={pending} data-testid="confirm-return">
        {pending ? "Processing…" : "Confirm return"}
      </Button>
    </form>
  );
}
