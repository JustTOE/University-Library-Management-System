"use client";

import { useState } from "react";
import { useActionState } from "react";
import { useRouter } from "next/navigation";

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
  { value: "ONLINE", label: "Online (card)" },
  { value: "CARD", label: "Card (tap to pay)" },
  { value: "CASH", label: "Cash" },
] as const;

export function PayFineButton({ fine }: { fine: FineResponse }) {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [method, setMethod] = useState<string>("ONLINE");
  const [state, dispatch, pending] = useActionState(
    payFineAction,
    initialActionState,
  );

  // "Online (card)" hands off to the dedicated card-payment page; the in-person
  // methods (POS tap / cash) are recorded immediately via payFineAction.
  const isOnline = method === "ONLINE";
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
            {isOnline
              ? `Continue to the secure card-payment page to pay ${formatMoney(fine.amount)}.`
              : `Record a ${formatMoney(fine.amount)} in-person payment.`}
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
              onValueChange={(value) => setMethod(value ?? "ONLINE")}
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
            {isOnline ? (
              <Button
                type="button"
                disabled={pending}
                onClick={() => {
                  setOpen(false);
                  router.push(`/my/fines/pay?fineId=${fine.id ?? ""}`);
                }}
              >
                Continue to card payment
              </Button>
            ) : (
              <Button type="submit" disabled={pending}>
                {pending ? "Processing…" : `Pay ${formatMoney(fine.amount)}`}
              </Button>
            )}
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
