"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import { returnLoan } from "@/lib/api/loans";
import { readSession } from "@/lib/auth/session";

import type { ActionState } from "./state";

export async function confirmReturnAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const loanId = Number(formData.get("loanId"));
  if (!Number.isFinite(loanId) || loanId <= 0) {
    return { status: "error", message: "Could not process this return." };
  }

  let status: string | undefined;
  try {
    const updated = await returnLoan(loanId, { token: session.token });
    status = updated.status;
  } catch (error) {
    return apiErrorToActionState(error, "Could not process this return.");
  }

  revalidatePath("/librarian/returns");
  return {
    status: "success",
    message: status
      ? `Returned. Status is now ${status}.`
      : "Loan returned.",
  };
}
