"use client";

import { useState } from "react";
import { useActionState } from "react";
import { useTranslations } from "next-intl";

import { deleteUserAction } from "@/app/(app)/admin/users/actions";
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

export function DeleteUserButton({
  userId,
  name,
}: {
  userId: number;
  name: string;
}) {
  const t = useTranslations("admin.users");
  const tCommon = useTranslations("common");
  const [open, setOpen] = useState(false);
  const [state, dispatch, pending] = useActionState(
    deleteUserAction,
    initialActionState,
  );
  const close = () => setOpen(false);
  useToastEffect(state, { onSuccess: close, onError: close });

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      <AlertDialogTrigger
        render={
          <Button
            size="sm"
            variant="destructive"
            disabled={pending}
            data-testid={`delete-user-${userId}`}
          >
            {tCommon("delete")}
          </Button>
        }
      />
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{t("deleteConfirmTitle", { name })}</AlertDialogTitle>
          <AlertDialogDescription>
            {t("deleteConfirmDescription")}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <form action={dispatch}>
          <input type="hidden" name="userId" value={userId} />
          <input type="hidden" name="name" value={name} />
          <AlertDialogFooter>
            <AlertDialogCancel>{t("keepUser")}</AlertDialogCancel>
            <AlertDialogAction
              type="submit"
              variant="destructive"
              disabled={pending}
            >
              {t("deleteUser")}
            </AlertDialogAction>
          </AlertDialogFooter>
        </form>
      </AlertDialogContent>
    </AlertDialog>
  );
}
