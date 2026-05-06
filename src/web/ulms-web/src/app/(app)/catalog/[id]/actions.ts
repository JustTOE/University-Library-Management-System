"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import type { ActionState } from "@/lib/api/action-state";
import { borrow } from "@/lib/api/loans";
import { createReservation } from "@/lib/api/reservations";
import { readSession } from "@/lib/auth/session";

function parseBookId(formData: FormData): number | null {
  const raw = formData.get("bookId");
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

export async function borrowAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session?.user.id) {
    return { status: "error", message: "Sign in again to borrow a book." };
  }
  const bookId = parseBookId(formData);
  if (bookId == null) {
    return { status: "error", message: "Invalid book." };
  }

  try {
    await borrow(
      { userId: session.user.id, bookId },
      { token: session.token },
    );
  } catch (error) {
    return apiErrorToActionState(error, "Could not borrow this book.");
  }

  revalidatePath(`/catalog/${bookId}`);
  return {
    status: "success",
    message: "Borrowed. See My loans for the due date.",
  };
}

export async function reserveAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session?.user.id) {
    return { status: "error", message: "Sign in again to reserve a book." };
  }
  const bookId = parseBookId(formData);
  if (bookId == null) {
    return { status: "error", message: "Invalid book." };
  }

  try {
    const reservation = await createReservation(
      { userId: session.user.id, bookId },
      { token: session.token },
    );
    revalidatePath(`/catalog/${bookId}`);
    const queueMsg =
      reservation.queuePosition && reservation.queuePosition > 1
        ? ` You are #${reservation.queuePosition} in the queue.`
        : "";
    return {
      status: "success",
      message: `Reserved.${queueMsg}`,
    };
  } catch (error) {
    const state = apiErrorToActionState(error, "Could not reserve this book.");
    if (
      state.status === "error" &&
      state.message.includes("conflicts with existing data")
    ) {
      return {
        ...state,
        message: "You already have an active reservation for this book.",
      };
    }
    return state;
  }
}
