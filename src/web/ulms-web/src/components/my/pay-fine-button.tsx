"use client";

import { useState } from "react";
import { useActionState } from "react";

import { payFineAction } from "@/app/(app)/my/fines/actions";
import { initialActionState } from "@/app/(app)/my/fines/state";
import { ErrorAlert } from "@/components/common/error-alert";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { FineResponse } from "@/lib/api/fines";
import { formatMoney } from "@/lib/format";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

const METHODS = [
  { value: "CARD", label: "Card" },
  { value: "ONLINE", label: "Online (bank transfer)" },
  { value: "CASH", label: "Cash" },
] as const;

export function PayFineButton({ fine }: { fine: FineResponse }) {
  const [open, setOpen] = useState(false);
  const [method, setMethod] = useState<string>("CARD");
  const [state, dispatch, pending] = useActionState(
    payFineAction,
    initialActionState,
  );
  // Keep dialog open on 402 declines so the inline alert can show; close on
  // success or any non-decline error (covered by the default toast).
  useToastEffect(state, {
    onSuccess: () => setOpen(false),
    suppressErrorToast: (s) =>
      s.status === "error" && Boolean(s.declineReason),
  });

  const declineReason =
    state.status === "error" ? state.declineReason : undefined;

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger
        render={
          <Button
            size="sm"
            disabled={pending}
            data-testid={`pay-fine-${fine.id}`}
          >
            Pay {formatMoney(fine.amount)}
          </Button>
        }
      />
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Pay fine</DialogTitle>
          <DialogDescription>
            Charge {formatMoney(fine.amount)} via the payment gateway.
          </DialogDescription>
        </DialogHeader>
        <form
          action={dispatch}
          className="flex flex-col gap-3"
        >
          <input type="hidden" name="fineId" value={fine.id ?? ""} />
          <input type="hidden" name="method" value={method} />
          <div className="flex flex-col gap-1.5">
            <label htmlFor="payment-method" className="text-sm font-medium">
              Method
            </label>
            <Select
              value={method}
              onValueChange={(value) => setMethod(value ?? "CARD")}
            >
              <SelectTrigger id="payment-method" className="w-full">
                <SelectValue placeholder="Choose a method" />
              </SelectTrigger>
              <SelectContent>
                {METHODS.map((m) => (
                  <SelectItem key={m.value} value={m.value}>
                    {m.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          {declineReason ? (
            <ErrorAlert
              title="Payment declined"
              message={declineReason}
            />
          ) : null}
          <DialogFooter>
            <DialogClose
              render={
                <Button variant="outline" type="button" disabled={pending}>
                  Cancel
                </Button>
              }
            />
            <Button type="submit" disabled={pending}>
              {pending ? "Processing…" : `Pay ${formatMoney(fine.amount)}`}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
