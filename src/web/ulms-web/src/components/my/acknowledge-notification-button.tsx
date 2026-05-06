"use client";

import { useActionState } from "react";

import { acknowledgeNotificationAction } from "@/app/(app)/my/notifications/actions";
import { initialActionState } from "@/app/(app)/my/notifications/state";
import { Button } from "@/components/ui/button";
import { useToastEffect } from "@/lib/hooks/use-toast-effect";

export function AcknowledgeNotificationButton({
  notificationId,
}: {
  notificationId: number;
}) {
  const [state, dispatch, pending] = useActionState(
    acknowledgeNotificationAction,
    initialActionState,
  );
  useToastEffect(state);

  return (
    <form action={dispatch}>
      <input type="hidden" name="notificationId" value={notificationId} />
      <Button
        type="submit"
        size="sm"
        variant="outline"
        disabled={pending}
        data-testid={`acknowledge-${notificationId}`}
      >
        {pending ? "Working…" : "Mark as read"}
      </Button>
    </form>
  );
}
