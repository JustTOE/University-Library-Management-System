"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import {
  initialActionState,
  loginAction,
  type ActionState,
} from "@/lib/auth/actions";

type Props = {
  redirectTo: string;
  expiredNotice: boolean;
  registeredNotice: boolean;
};

function formatLockedUntil(iso?: string): string | null {
  if (!iso) return null;
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return null;
  return d.toLocaleString();
}

export function LoginForm({ redirectTo, expiredNotice, registeredNotice }: Props) {
  const [state, formAction, isPending] = useActionState<ActionState, FormData>(
    loginAction,
    initialActionState,
  );

  const lockedUntil =
    state.status === "error" ? formatLockedUntil(state.lockedUntil) : null;
  const fieldErrors = state.status === "error" ? state.fieldErrors ?? {} : {};
  const topError =
    state.status === "error" && Object.keys(fieldErrors).length === 0
      ? state.message
      : null;

  return (
    <form action={formAction} className="flex flex-col gap-4" noValidate>
      <input type="hidden" name="redirectTo" value={redirectTo} />

      {expiredNotice && (
        <p className="rounded-md border border-border bg-muted px-3 py-2 text-sm text-muted-foreground">
          Your session expired. Please sign in again.
        </p>
      )}
      {registeredNotice && (
        <p className="rounded-md border border-border bg-muted px-3 py-2 text-sm text-muted-foreground">
          Account created. Sign in below to continue.
        </p>
      )}

      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="email">Email</FieldLabel>
          <Input
            id="email"
            name="email"
            type="email"
            autoComplete="username"
            required
            aria-invalid={Boolean(fieldErrors.email) || undefined}
          />
          {fieldErrors.email && (
            <FieldError>{fieldErrors.email}</FieldError>
          )}
        </Field>

        <Field>
          <FieldLabel htmlFor="password">Password</FieldLabel>
          <Input
            id="password"
            name="password"
            type="password"
            autoComplete="current-password"
            required
            aria-invalid={Boolean(fieldErrors.password) || undefined}
          />
          {fieldErrors.password && (
            <FieldError>{fieldErrors.password}</FieldError>
          )}
        </Field>
      </FieldGroup>

      {topError && (
        <FieldDescription className="text-destructive">
          {lockedUntil
            ? `${topError} Try again after ${lockedUntil}.`
            : topError}
        </FieldDescription>
      )}

      <Button type="submit" disabled={isPending} className="w-full">
        {isPending ? "Signing in…" : "Sign in"}
      </Button>
    </form>
  );
}
