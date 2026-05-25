"use client";

import { useActionState, useMemo, useState } from "react";
import { useRouter } from "next/navigation";

import { checkoutAction } from "@/app/(app)/my/fines/pay/actions";
import { initialCheckoutState } from "@/app/(app)/my/fines/pay/state";
import { ErrorAlert } from "@/components/common/error-alert";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import type { FineResponse } from "@/lib/api/fines";
import { formatDateOnly, formatMoney } from "@/lib/format";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

type CardErrors = Partial<
  Record<"number" | "expiry" | "cvc" | "name", string>
>;

export function CheckoutForm({
  fines,
  preselectId,
}: {
  fines: FineResponse[];
  preselectId?: number;
}) {
  const router = useRouter();
  const [state, dispatch, pending] = useActionState(
    checkoutAction,
    initialCheckoutState,
  );

  // Selected fine IDs. Default: preselect one if given, otherwise select all.
  const [selected, setSelected] = useState<Set<number>>(() => {
    if (preselectId && fines.some((f) => f.id === preselectId)) {
      return new Set([preselectId]);
    }
    return new Set(
      fines.map((f) => f.id).filter((id): id is number => id != null),
    );
  });

  const [number, setNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvc, setCvc] = useState("");
  const [name, setName] = useState("");
  const [cardErrors, setCardErrors] = useState<CardErrors>({});

  useToastEffect(state, {
    onSuccess: () => router.push("/my/fines"),
    // Decline reasons are shown inline; skip the default error toast for them.
    suppressErrorToast: (s) =>
      s.status === "error" && Boolean(s.declineReason),
  });

  const selectedFines = fines.filter((f) => f.id != null && selected.has(f.id));
  const total = useMemo(
    () => selectedFines.reduce((sum, f) => sum + (f.amount ?? 0), 0),
    [selectedFines],
  );
  const allSelected = selected.size === fines.length && fines.length > 0;

  function toggle(id: number) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function toggleAll() {
    setSelected(() => {
      if (allSelected) return new Set();
      return new Set(
        fines.map((f) => f.id).filter((id): id is number => id != null),
      );
    });
  }

  // Validates the card fields client-side. These checks are cosmetic for now
  // (the mock gateway decides approval by amount); they move server-side when
  // a real gateway is integrated.
  function handleSubmit(formData: FormData) {
    const errors = validateCard({ number, expiry, cvc, name });
    if (Object.keys(errors).length > 0) {
      setCardErrors(errors);
      return; // do not dispatch — keep the user on the form
    }
    setCardErrors({});
    formData.set("fineIds", [...selected].join(","));
    dispatch(formData);
  }

  const declineReason =
    state.status === "error" ? state.declineReason : undefined;
  const fieldFineError =
    state.status === "error" ? state.fieldErrors?.fineIds : undefined;

  if (fines.length === 0) return null;

  return (
    <form action={handleSubmit} className="flex flex-col gap-6" noValidate>
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Select fines to pay</CardTitle>
          <CardDescription>
            Choose the unpaid fines to settle in this transaction.
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          <label className="flex items-center gap-2 border-b border-border pb-3 text-sm font-medium">
            <Checkbox
              checked={allSelected}
              onCheckedChange={toggleAll}
              aria-label="Select all fines"
            />
            Select all
          </label>
          <ul className="flex flex-col gap-2">
            {fines.map((fine) => {
              const id = fine.id;
              if (id == null) return null;
              return (
                <li key={id}>
                  <label className="flex items-center justify-between gap-3 rounded-md border border-border px-3 py-2 text-sm">
                    <span className="flex items-center gap-3">
                      <Checkbox
                        checked={selected.has(id)}
                        onCheckedChange={() => toggle(id)}
                        aria-label={`Pay fine for loan ${fine.loanId ?? id}`}
                      />
                      <span className="flex flex-col">
                        <span className="font-medium">
                          Loan #{fine.loanId ?? "—"}
                        </span>
                        <span className="text-xs text-muted-foreground">
                          Calculated {formatDateOnly(fine.calculatedDate)}
                        </span>
                      </span>
                    </span>
                    <span className="font-medium">
                      {formatMoney(fine.amount)}
                    </span>
                  </label>
                </li>
              );
            })}
          </ul>
          {fieldFineError ? (
            <p className="text-sm text-destructive">{fieldFineError}</p>
          ) : null}
          <div className="flex items-center justify-between border-t border-border pt-3 text-sm">
            <span className="text-muted-foreground">Total to pay</span>
            <span className="text-lg font-semibold">{formatMoney(total)}</span>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Card details</CardTitle>
          <CardDescription>
            Enter your card to pay {formatMoney(total)}. Test mode — no real
            charge is made.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="card-name">Cardholder name</FieldLabel>
              <Input
                id="card-name"
                name="cardName"
                autoComplete="cc-name"
                placeholder="Jane Doe"
                value={name}
                onChange={(e) => setName(e.target.value)}
                aria-invalid={Boolean(cardErrors.name) || undefined}
              />
              {cardErrors.name ? (
                <FieldError>{cardErrors.name}</FieldError>
              ) : null}
            </Field>
            <Field>
              <FieldLabel htmlFor="card-number">Card number</FieldLabel>
              <Input
                id="card-number"
                name="cardNumber"
                inputMode="numeric"
                autoComplete="cc-number"
                placeholder="4242 4242 4242 4242"
                value={number}
                onChange={(e) => setNumber(formatCardNumber(e.target.value))}
                aria-invalid={Boolean(cardErrors.number) || undefined}
              />
              {cardErrors.number ? (
                <FieldError>{cardErrors.number}</FieldError>
              ) : null}
            </Field>
            <div className="grid grid-cols-2 gap-4">
              <Field>
                <FieldLabel htmlFor="card-expiry">Expiry (MM/YY)</FieldLabel>
                <Input
                  id="card-expiry"
                  name="cardExpiry"
                  inputMode="numeric"
                  autoComplete="cc-exp"
                  placeholder="12/29"
                  value={expiry}
                  onChange={(e) => setExpiry(formatExpiry(e.target.value))}
                  aria-invalid={Boolean(cardErrors.expiry) || undefined}
                />
                {cardErrors.expiry ? (
                  <FieldError>{cardErrors.expiry}</FieldError>
                ) : null}
              </Field>
              <Field>
                <FieldLabel htmlFor="card-cvc">CVC</FieldLabel>
                <Input
                  id="card-cvc"
                  name="cardCvc"
                  inputMode="numeric"
                  autoComplete="cc-csc"
                  placeholder="123"
                  value={cvc}
                  onChange={(e) =>
                    setCvc(e.target.value.replace(/\D/g, "").slice(0, 4))
                  }
                  aria-invalid={Boolean(cardErrors.cvc) || undefined}
                />
                {cardErrors.cvc ? (
                  <FieldError>{cardErrors.cvc}</FieldError>
                ) : null}
              </Field>
            </div>
          </FieldGroup>
        </CardContent>
      </Card>

      {declineReason ? (
        <ErrorAlert title="Payment declined" message={declineReason} />
      ) : null}

      <div className="flex items-center justify-end gap-3">
        <Button
          type="submit"
          disabled={pending || selected.size === 0}
          data-testid="checkout-submit"
        >
          {pending ? "Processing…" : `Pay ${formatMoney(total)}`}
        </Button>
      </div>
    </form>
  );
}

// ---- Card formatting + validation helpers (client-side only) ----

function formatCardNumber(value: string): string {
  const digits = value.replace(/\D/g, "").slice(0, 19);
  return digits.replace(/(.{4})/g, "$1 ").trim();
}

function formatExpiry(value: string): string {
  const digits = value.replace(/\D/g, "").slice(0, 4);
  if (digits.length <= 2) return digits;
  return `${digits.slice(0, 2)}/${digits.slice(2)}`;
}

function validateCard(input: {
  number: string;
  expiry: string;
  cvc: string;
  name: string;
}): CardErrors {
  const errors: CardErrors = {};

  if (!input.name.trim()) {
    errors.name = "Enter the cardholder name.";
  }

  const digits = input.number.replace(/\D/g, "");
  if (digits.length < 13 || digits.length > 19 || !luhnValid(digits)) {
    errors.number = "Enter a valid card number.";
  }

  const expiryMatch = /^(\d{2})\/(\d{2})$/.exec(input.expiry);
  if (!expiryMatch) {
    errors.expiry = "Use MM/YY.";
  } else {
    const month = Number(expiryMatch[1]);
    const year = 2000 + Number(expiryMatch[2]);
    if (month < 1 || month > 12) {
      errors.expiry = "Invalid month.";
    } else {
      // Card is valid through the end of its expiry month.
      const now = new Date();
      const endOfMonth = new Date(year, month, 0, 23, 59, 59);
      if (endOfMonth < now) errors.expiry = "Card has expired.";
    }
  }

  if (!/^\d{3,4}$/.test(input.cvc)) {
    errors.cvc = "3–4 digits.";
  }

  return errors;
}

function luhnValid(digits: string): boolean {
  let sum = 0;
  let double = false;
  for (let i = digits.length - 1; i >= 0; i--) {
    let d = digits.charCodeAt(i) - 48;
    if (double) {
      d *= 2;
      if (d > 9) d -= 9;
    }
    sum += d;
    double = !double;
  }
  return sum % 10 === 0;
}
