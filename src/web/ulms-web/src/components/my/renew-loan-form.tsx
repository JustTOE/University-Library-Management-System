"use client";

import { useActionState } from "react";

import { renewLoanAction } from "@/app/(app)/my/loans/actions";
import { initialActionState } from "@/app/(app)/my/loans/state";
import { Button } from "@/components/ui/button";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

export function RenewLoanForm({
  loanId,
  disabled,
}: {
  loanId: number;
  disabled?: boolean;
}) {
  const [state, dispatch, pending] = useActionState(
    renewLoanAction,
    initialActionState,
  );
  useToastEffect(state);

  return (
    <form action={dispatch}>
      <input type="hidden" name="loanId" value={loanId} />
      <Button
        type="submit"
        size="sm"
        variant="outline"
        disabled={pending || disabled}
        data-testid={`renew-${loanId}`}
      >
        {pending ? "Working…" : "Renew"}
      </Button>
    </form>
  );
}
