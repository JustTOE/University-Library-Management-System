"use server";

import { revalidatePath } from "next/cache";

import { updateBook } from "@/lib/api/books";
import { readSession } from "@/lib/auth/session";

import { mapBookError } from "../../book-error";
import { readBookFormBody } from "../../form-utils";
import type { ActionState } from "../../state";

export async function updateBookAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const bookId = Number(formData.get("bookId"));
  if (!Number.isFinite(bookId) || bookId <= 0) {
    return { status: "error", message: "Could not save the book." };
  }

  const body = readBookFormBody(formData);

  try {
    await updateBook(bookId, body, { token: session.token });
  } catch (error) {
    return mapBookError(error, "Could not save the book.");
  }

  revalidatePath("/librarian/catalog");
  revalidatePath(`/librarian/catalog/${bookId}/edit`);
  return { status: "success", message: "Saved." };
}
