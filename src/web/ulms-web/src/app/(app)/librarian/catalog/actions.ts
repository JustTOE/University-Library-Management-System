"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import { deleteBook } from "@/lib/api/books";
import { readSession } from "@/lib/auth/session";

import type { ActionState } from "./state";

export async function deleteBookAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const bookId = Number(formData.get("bookId"));
  const title = String(formData.get("title") ?? "this book");
  if (!Number.isFinite(bookId) || bookId <= 0) {
    return { status: "error", message: "Could not delete this book." };
  }

  try {
    await deleteBook(bookId, { token: session.token });
  } catch (error) {
    return apiErrorToActionState(error, "Could not delete this book.");
  }

  revalidatePath("/librarian/catalog");
  return { status: "success", message: `Deleted "${title}".` };
}
