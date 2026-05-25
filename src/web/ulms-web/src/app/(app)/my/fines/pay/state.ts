/**
 * Checkout-specific action state. Extends the shared ActionState shape with
 * the per-fine outcome the multi-fine checkout produces (which fines were
 * paid, which one was declined). Kept free of "server-only" imports so the
 * client CheckoutForm can read the type via useActionState.
 */

export type CheckoutState =
  | { status: "idle" }
  | {
      status: "error";
      message: string;
      fieldErrors?: Record<string, string>;
      declineReason?: string;
      declinedFineId?: number;
      paidFineIds?: number[];
    }
  | { status: "success"; message: string; paidFineIds?: number[] };

export const initialCheckoutState: CheckoutState = { status: "idle" };
