"use server";

import { revalidatePath } from "next/cache";

import { apiErrorToActionState } from "@/lib/api/action-helpers";
import { ApiError } from "@/lib/api/client";
import type { PaymentRequest } from "@/lib/api/payments";
import { processPayment } from "@/lib/api/payments";
import { readSession } from "@/lib/auth/session";

import type { CheckoutState } from "./state";

/**
 * Online card checkout. The backend charges one fine per request
 * (POST /api/payments), so a multi-fine checkout charges each selected fine
 * sequentially and aggregates the outcome — earlier fines can succeed while a
 * later one is declined.
 *
 * Card fields collected by the form are NOT forwarded: the payments API only
 * accepts a `method`, and a real gateway would tokenise the card client-side.
 * Today the mock gateway approves CARD charges by amount. The card inputs are
 * UI realism + client-side validation only; when a real gateway is wired in,
 * tokenisation/charging moves server-side here.
 */
export async function checkoutAction(
  _prev: CheckoutState,
  formData: FormData,
): Promise<CheckoutState> {
  const session = await readSession();
  if (!session?.user.id) {
    return { status: "error", message: "Sign in again to pay your fines." };
  }
  const userId = session.user.id;
  const token = session.token;

  const fineIds = parseFineIds(formData.get("fineIds"));
  if (fineIds.length === 0) {
    return {
      status: "error",
      message: "Select at least one fine to pay.",
      fieldErrors: { fineIds: "Choose one or more fines." },
    };
  }

  const method: PaymentRequest["method"] = "CARD";
  const paidFineIds: number[] = [];
  let declineReason: string | undefined;
  let declinedFineId: number | undefined;

  for (const fineId of fineIds) {
    try {
      await processPayment({ fineId, userId, method }, { token });
      paidFineIds.push(fineId);
    } catch (error) {
      // A declined charge (HTTP 402) carries a declineReason. Stop on the
      // first failure: the remaining fines stay UNPAID and can be retried.
      if (error instanceof ApiError && error.declineReason) {
        declineReason = error.declineReason;
        declinedFineId = fineId;
        break;
      }
      // Non-decline failure (network, 4xx/5xx): surface it, but keep any
      // already-paid fines reflected in the result.
      const mapped = apiErrorToActionState(error, "Could not process payment.");
      revalidate();
      return {
        status: "error",
        message:
          mapped.status === "error"
            ? mapped.message
            : "Could not process payment.",
        fieldErrors: mapped.status === "error" ? mapped.fieldErrors : undefined,
        paidFineIds,
        declinedFineId: fineId,
      };
    }
  }

  revalidate();

  if (declineReason) {
    const paidNote =
      paidFineIds.length > 0
        ? ` ${paidFineIds.length} fine${paidFineIds.length > 1 ? "s" : ""} paid before the decline.`
        : "";
    return {
      status: "error",
      message: `Payment declined.${paidNote}`,
      declineReason,
      declinedFineId,
      paidFineIds,
    };
  }

  const count = paidFineIds.length;
  return {
    status: "success",
    message:
      count > 1
        ? `Paid ${count} fines.`
        : "Payment completed.",
    paidFineIds,
  };
}

function parseFineIds(raw: FormDataEntryValue | null): number[] {
  if (typeof raw !== "string" || raw.trim() === "") return [];
  const ids = raw
    .split(",")
    .map((part) => Number(part.trim()))
    .filter((n) => Number.isInteger(n) && n > 0);
  // De-duplicate while preserving order.
  return [...new Set(ids)];
}

function revalidate() {
  revalidatePath("/my/fines");
  revalidatePath("/my/fines/pay");
}
