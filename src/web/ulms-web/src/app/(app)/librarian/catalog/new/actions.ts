"use server";

import { revalidatePath } from "next/cache";

import { createBook } from "@/lib/api/books";
import { readSession } from "@/lib/auth/session";

import { mapBookError } from "../book-error";
import { readBookFormBody } from "../form-utils";
import type { ActionState } from "../state";

export async function createBookAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const body = readBookFormBody(formData);

  try {
    await createBook(body, { token: session.token });
  } catch (error) {
    return mapBookError(error, "Could not save the book.");
  }

  revalidatePath("/librarian/catalog");
  return { status: "success", message: "Book created." };
}
