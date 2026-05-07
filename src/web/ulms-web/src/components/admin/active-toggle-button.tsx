"use client";

import { useState } from "react";
import { useActionState } from "react";
import { useTranslations } from "next-intl";

import { setActiveAction } from "@/app/(app)/admin/users/actions";
import { initialActionState } from "@/app/(app)/admin/users/state";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

export function ActiveToggleButton({
  userId,
  name,
  isActive,
}: {
  userId: number;
  name: string;
  isActive: boolean;
}) {
  const t = useTranslations("admin.users");
  const tCommon = useTranslations("common");
  const [open, setOpen] = useState(false);
  const [state, dispatch, pending] = useActionState(
    setActiveAction,
    initialActionState,
  );
  const close = () => setOpen(false);
  useToastEffect(state, { onSuccess: close, onError: close });

  const willActivate = !isActive;

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      <AlertDialogTrigger
        render={
          <Button
            size="sm"
            variant={willActivate ? "outline" : "secondary"}
            disabled={pending}
            data-testid={`toggle-active-${userId}`}
          >
            {willActivate ? t("reactivate") : t("deactivate")}
          </Button>
        }
      />
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            {willActivate
              ? t("reactivateConfirmTitle", { name })
              : t("deactivateConfirmTitle", { name })}
          </AlertDialogTitle>
          <AlertDialogDescription>
            {willActivate
              ? t("reactivateConfirmDescription")
              : t("deactivateConfirmDescription")}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <form action={dispatch}>
          <input type="hidden" name="userId" value={userId} />
          <input
            type="hidden"
            name="nextActive"
            value={willActivate ? "true" : "false"}
          />
          <AlertDialogFooter>
            <AlertDialogCancel>{tCommon("cancel")}</AlertDialogCancel>
            <AlertDialogAction type="submit" disabled={pending}>
              {willActivate ? t("reactivate") : t("deactivate")}
            </AlertDialogAction>
          </AlertDialogFooter>
        </form>
      </AlertDialogContent>
    </AlertDialog>
  );
}
