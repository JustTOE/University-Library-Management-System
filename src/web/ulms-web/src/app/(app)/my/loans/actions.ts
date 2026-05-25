"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import type { ActionState } from "@/lib/api/action-state";
import { renew, reportLost } from "@/lib/api/loans";
import { readSession } from "@/lib/auth/session";

export async function renewLoanAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to renew." };
  }
  const loanId = Number(formData.get("loanId"));
  if (!Number.isInteger(loanId) || loanId <= 0) {
    return { status: "error", message: "Invalid loan." };
  }

  let loan;
  try {
    loan = await renew(loanId, { token: session.token });
  } catch (error) {
    return apiErrorToActionState(error, "Could not renew this loan.");
  }

  revalidatePath("/my/loans");
  return {
    status: "success",
    message: loan.dueDate
      ? `Renewed. New due date: ${loan.dueDate}.`
      : "Loan renewed.",
  };
}

export async function reportLostAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to report a book." };
  }
  const loanId = Number(formData.get("loanId"));
  if (!Number.isInteger(loanId) || loanId <= 0) {
    return { status: "error", message: "Invalid loan." };
  }

  try {
    await reportLost(loanId, { token: session.token });
  } catch (error) {
    return apiErrorToActionState(error, "Could not report this book as lost.");
  }

  revalidatePath("/my/loans");
  revalidatePath("/my/fines");
  return {
    status: "success",
    message: "Reported as lost. A replacement fee has been added to your fines.",
  };
}
