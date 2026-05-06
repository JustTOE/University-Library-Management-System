"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import type { ActionState } from "@/lib/api/action-state";
import type { PaymentRequest } from "@/lib/api/payments";
import { processPayment } from "@/lib/api/payments";
import { readSession } from "@/lib/auth/session";

const ALLOWED_METHODS: ReadonlySet<PaymentRequest["method"]> = new Set([
  "CASH",
  "CARD",
  "ONLINE",
]);

export async function payFineAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session?.user.id) {
    return { status: "error", message: "Sign in again to pay a fine." };
  }

  const fineId = Number(formData.get("fineId"));
  const method = String(formData.get("method") ?? "") as PaymentRequest["method"];

  if (!Number.isInteger(fineId) || fineId <= 0) {
    return { status: "error", message: "Invalid fine." };
  }
  if (!ALLOWED_METHODS.has(method)) {
    return {
      status: "error",
      message: "Choose a payment method.",
      fieldErrors: { method: "Required" },
    };
  }

  try {
    await processPayment(
      { fineId, userId: session.user.id, method },
      { token: session.token },
    );
  } catch (error) {
    return apiErrorToActionState(error, "Could not process this payment.");
  }

  revalidatePath("/my/fines");
  return { status: "success", message: "Payment completed." };
}
