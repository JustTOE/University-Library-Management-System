"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import type { ActionState } from "@/lib/api/action-state";
import { cancelReservation } from "@/lib/api/reservations";
import { readSession } from "@/lib/auth/session";

export async function cancelReservationAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to cancel." };
  }
  const reservationId = Number(formData.get("reservationId"));
  if (!Number.isInteger(reservationId) || reservationId <= 0) {
    return { status: "error", message: "Invalid reservation." };
  }

  try {
    await cancelReservation(reservationId, { token: session.token });
  } catch (error) {
    return apiErrorToActionState(
      error,
      "Could not cancel this reservation.",
    );
  }

  revalidatePath("/my/reservations");
  return { status: "success", message: "Reservation cancelled." };
}
