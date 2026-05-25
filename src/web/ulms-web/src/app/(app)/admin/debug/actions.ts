"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import { makeLoanOverdue } from "@/lib/api/loans";
import { readSession } from "@/lib/auth/session";

import type { ActionState } from "./state";

export async function makeOverdueAction(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const session = await readSession();
  if (!session) {
    return { status: "error", message: "Sign in again to continue." };
  }

  const loanId = Number(formData.get("loanId"));
  const days = Number(formData.get("days"));
  if (!Number.isFinite(loanId) || loanId <= 0) {
    return { status: "error", message: "Invalid loan." };
  }
  if (!Number.isInteger(days) || days < 1) {
    return { status: "error", message: "Days overdue must be a whole number ≥ 1." };
  }

  try {
    const fine = await makeLoanOverdue(loanId, days, { token: session.token });
    revalidatePath("/admin/debug");
    return {
      status: "success",
      message: `Loan #${loanId} is now ${days} day(s) overdue — €${fine.amount} fine created.`,
    };
  } catch (error) {
    return apiErrorToActionState(error, "Could not make this loan overdue.");
  }
}
