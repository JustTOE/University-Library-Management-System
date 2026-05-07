"use server";

import { revalidatePath } from "next/cache";

import { createUser } from "@/lib/api/users";
import { readSession } from "@/lib/auth/session";

import { readUserFormBody } from "../form-utils";
import { mapUserError } from "../user-error";

import type { ActionState } from "./state";

export async function createUserAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const body = readUserFormBody(formData, "create");

  try {
    await createUser(body, { token: session.token });
  } catch (error) {
    return mapUserError(error, "Could not create the user.");
  }

  revalidatePath("/admin/users");
  return { status: "success", message: "User created." };
}
