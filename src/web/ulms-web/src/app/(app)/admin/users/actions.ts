"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import { activateUser, deactivateUser, deleteUser } from "@/lib/api/users";
import { readSession } from "@/lib/auth/session";

import type { ActionState } from "./state";

export async function setActiveAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const userId = Number(formData.get("userId"));
  const nextActive = formData.get("nextActive") === "true";
  if (!Number.isFinite(userId) || userId <= 0) {
    return { status: "error", message: "Could not change this user." };
  }

  try {
    if (nextActive) {
      await activateUser(userId, { token: session.token });
    } else {
      await deactivateUser(userId, { token: session.token });
    }
  } catch (error) {
    return apiErrorToActionState(
      error,
      nextActive
        ? "Could not reactivate this user."
        : "Could not deactivate this user.",
    );
  }

  revalidatePath("/admin/users");
  return {
    status: "success",
    message: nextActive ? "User reactivated." : "User deactivated.",
  };
}

export async function deleteUserAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const userId = Number(formData.get("userId"));
  const name = String(formData.get("name") ?? "this user");
  if (!Number.isFinite(userId) || userId <= 0) {
    return { status: "error", message: "Could not anonymise this user." };
  }

  try {
    await deleteUser(userId, { token: session.token });
  } catch (error) {
    return apiErrorToActionState(error, "Could not anonymise this user.");
  }

  revalidatePath("/admin/users");
  return { status: "success", message: `Anonymised ${name}.` };
}
