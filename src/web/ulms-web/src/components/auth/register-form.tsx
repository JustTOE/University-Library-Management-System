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
import { registerAction } from "@/lib/auth/actions";
import { initialActionState, type ActionState } from "@/lib/auth/state";

export function RegisterForm() {
  const [state, formAction, isPending] = useActionState<ActionState, FormData>(
    registerAction,
    initialActionState,
  );

  const fieldErrors = state.status === "error" ? state.fieldErrors ?? {} : {};
  const topError =
    state.status === "error" && Object.keys(fieldErrors).length === 0
      ? state.message
      : null;

  return (
    <form action={formAction} className="flex flex-col gap-4" noValidate>
      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="name">Full name</FieldLabel>
          <Input
            id="name"
            name="name"
            autoComplete="name"
            required
            aria-invalid={Boolean(fieldErrors.name) || undefined}
          />
          {fieldErrors.name && <FieldError>{fieldErrors.name}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="email">University email</FieldLabel>
          <Input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            required
            aria-invalid={Boolean(fieldErrors.email) || undefined}
          />
          {fieldErrors.email && <FieldError>{fieldErrors.email}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="universityId">University ID</FieldLabel>
          <Input
            id="universityId"
            name="universityId"
            required
            aria-invalid={Boolean(fieldErrors.universityId) || undefined}
          />
          {fieldErrors.universityId && (
            <FieldError>{fieldErrors.universityId}</FieldError>
          )}
        </Field>

        <Field>
          <FieldLabel htmlFor="phone">
            Phone <span className="text-muted-foreground">(optional)</span>
          </FieldLabel>
          <Input
            id="phone"
            name="phone"
            type="tel"
            autoComplete="tel"
            aria-invalid={Boolean(fieldErrors.phone) || undefined}
          />
          {fieldErrors.phone && <FieldError>{fieldErrors.phone}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="password">Password</FieldLabel>
          <Input
            id="password"
            name="password"
            type="password"
            autoComplete="new-password"
            minLength={8}
            required
            aria-invalid={Boolean(fieldErrors.password) || undefined}
            aria-describedby="register-password-hint"
          />
          <FieldDescription id="register-password-hint">At least 8 characters.</FieldDescription>
          {fieldErrors.password && (
            <FieldError>{fieldErrors.password}</FieldError>
          )}
        </Field>
      </FieldGroup>

      {topError && (
        <FieldDescription className="text-destructive">
          {topError}
        </FieldDescription>
      )}

      <Button type="submit" disabled={isPending} className="w-full">
        {isPending ? "Creating account…" : "Create account"}
      </Button>
    </form>
  );
}
