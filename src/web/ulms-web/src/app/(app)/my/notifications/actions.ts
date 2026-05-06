"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import type { ActionState } from "@/lib/api/action-state";
import { acknowledgeNotification } from "@/lib/api/notifications";
import { readSession } from "@/lib/auth/session";

export async function acknowledgeNotificationAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again." };
  }

  const notificationId = Number(formData.get("notificationId"));
  if (!Number.isInteger(notificationId) || notificationId <= 0) {
    return { status: "error", message: "Invalid notification." };
  }

  try {
    await acknowledgeNotification(notificationId, { token: session.token });
  } catch (error) {
    return apiErrorToActionState(error, "Could not acknowledge.");
  }

  revalidatePath("/my/notifications");
  return { status: "success", message: "Marked as read." };
}
