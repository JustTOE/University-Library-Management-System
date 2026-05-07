"use server";

import { revalidatePath } from "next/cache";

import { updateUser } from "@/lib/api/users";
import { readSession } from "@/lib/auth/session";

import { readUserFormBody } from "../../form-utils";
import { mapUserError } from "../../user-error";

import type { ActionState } from "./state";

export async function updateUserAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const userId = Number(formData.get("userId"));
  if (!Number.isFinite(userId) || userId <= 0) {
    return { status: "error", message: "Could not save this user." };
  }

  const body = readUserFormBody(formData, "edit");

  try {
    await updateUser(userId, body, { token: session.token });
  } catch (error) {
    return mapUserError(error, "Could not save this user.");
  }

  revalidatePath("/admin/users");
  revalidatePath(`/admin/users/${userId}`);
  return { status: "success", message: "Saved." };
}
