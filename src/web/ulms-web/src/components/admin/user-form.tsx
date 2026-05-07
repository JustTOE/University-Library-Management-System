"use client";

import { useActionState, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useTranslations } from "next-intl";

import { createUserAction } from "@/app/(app)/admin/users/new/actions";
import { updateUserAction } from "@/app/(app)/admin/users/[id]/edit/actions";
import {
  initialActionState,
  type ActionState,
} from "@/app/(app)/admin/users/state";
import { Button, buttonVariants } from "@/components/ui/button";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import type { UserResponse } from "@/lib/api/users";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

type UserFormProps =
  | { mode: "create"; initial?: undefined; userId?: undefined }
  | { mode: "edit"; initial: UserResponse; userId: number };

type Role = "STUDENT" | "LIBRARIAN" | "ADMIN";

function defaultsFrom(initial: UserResponse | undefined) {
  if (!initial) {
    return {
      name: "",
      email: "",
      universityId: "",
      staffId: "",
      phone: "",
      role: "STUDENT" as Role,
    };
  }
  return {
    name: initial.name ?? "",
    email: initial.email ?? "",
    universityId: initial.universityId ?? "",
    staffId: initial.staffId ?? "",
    phone: initial.phone ?? "",
    role: (initial.role ?? "STUDENT") as Role,
  };
}

export function UserForm(props: UserFormProps) {
  const router = useRouter();
  const t = useTranslations("admin.users");
  const tFields = useTranslations("admin.users.fields");
  const tSubmit = useTranslations("admin.users.submit");
  const tCommon = useTranslations("common");
  const action = props.mode === "create" ? createUserAction : updateUserAction;
  const [state, dispatch, pending] = useActionState<ActionState, FormData>(
    action,
    initialActionState,
  );

  const defaults = defaultsFrom(props.initial);
  const [role, setRole] = useState<Role>(defaults.role);

  const fieldErrors = state.status === "error" ? state.fieldErrors ?? {} : {};
  const topError =
    state.status === "error" && Object.keys(fieldErrors).length === 0
      ? state.message
      : null;

  useToastEffect(state, {
    onSuccess: () => router.push("/admin/users"),
  });

  const isStudent = role === "STUDENT";

  const ROLES: { value: Role; label: string }[] = [
    { value: "STUDENT", label: t("roleStudent") },
    { value: "LIBRARIAN", label: t("roleLibrarian") },
    { value: "ADMIN", label: t("roleAdmin") },
  ];

  return (
    <form action={dispatch} className="flex flex-col gap-4" noValidate>
      {props.mode === "edit" ? (
        <input type="hidden" name="userId" value={props.userId} />
      ) : null}

      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="name">{tFields("name")}</FieldLabel>
          <Input
            id="name"
            name="name"
            autoComplete="name"
            required
            defaultValue={defaults.name}
            aria-invalid={Boolean(fieldErrors.name) || undefined}
          />
          {fieldErrors.name && <FieldError>{fieldErrors.name}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="email">{tFields("email")}</FieldLabel>
          <Input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            required
            defaultValue={defaults.email}
            aria-invalid={Boolean(fieldErrors.email) || undefined}
          />
          {fieldErrors.email && <FieldError>{fieldErrors.email}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="role">{tFields("role")}</FieldLabel>
          <select
            id="role"
            name="role"
            value={role}
            onChange={(e) => setRole(e.currentTarget.value as Role)}
            required
            className="h-9 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-1 focus-visible:outline-ring"
            aria-invalid={Boolean(fieldErrors.role) || undefined}
          >
            {ROLES.map((r) => (
              <option key={r.value} value={r.value}>
                {r.label}
              </option>
            ))}
          </select>
          {fieldErrors.role && <FieldError>{fieldErrors.role}</FieldError>}
        </Field>

        {isStudent ? (
          <Field>
            <FieldLabel htmlFor="universityId">{tFields("universityId")}</FieldLabel>
            <Input
              id="universityId"
              name="universityId"
              required
              defaultValue={defaults.universityId}
              aria-invalid={Boolean(fieldErrors.universityId) || undefined}
            />
            {fieldErrors.universityId && (
              <FieldError>{fieldErrors.universityId}</FieldError>
            )}
          </Field>
        ) : (
          <Field>
            <FieldLabel htmlFor="staffId">{tFields("staffId")}</FieldLabel>
            <Input
              id="staffId"
              name="staffId"
              required
              defaultValue={defaults.staffId}
              aria-invalid={Boolean(fieldErrors.staffId) || undefined}
            />
            {fieldErrors.staffId && (
              <FieldError>{fieldErrors.staffId}</FieldError>
            )}
          </Field>
        )}

        <Field>
          <FieldLabel htmlFor="phone">{tFields("phoneOptional")}</FieldLabel>
          <Input
            id="phone"
            name="phone"
            type="tel"
            autoComplete="tel"
            defaultValue={defaults.phone}
            aria-invalid={Boolean(fieldErrors.phone) || undefined}
          />
          {fieldErrors.phone && <FieldError>{fieldErrors.phone}</FieldError>}
        </Field>

        <Field>
          <FieldLabel htmlFor="password">
            {tFields("password")}
            {props.mode === "edit" ? (
              <span className="text-muted-foreground">
                {" "}
                {tFields("passwordOptionalEdit")}
              </span>
            ) : null}
          </FieldLabel>
          <Input
            id="password"
            name="password"
            type="password"
            autoComplete={props.mode === "create" ? "new-password" : "off"}
            minLength={8}
            required={props.mode === "create"}
            aria-invalid={Boolean(fieldErrors.password) || undefined}
            aria-describedby="password-hint"
          />
          <FieldDescription id="password-hint">
            {props.mode === "create"
              ? tFields("passwordHintCreate")
              : tFields("passwordHintEdit")}
          </FieldDescription>
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

      <div className="flex items-center justify-end gap-2">
        <Link
          href="/admin/users"
          className={buttonVariants({ variant: "ghost" })}
        >
          {tCommon("cancel")}
        </Link>
        <Button type="submit" disabled={pending}>
          {pending
            ? tCommon("saving")
            : props.mode === "create"
              ? tSubmit("create")
              : tSubmit("save")}
        </Button>
      </div>
    </form>
  );
}
