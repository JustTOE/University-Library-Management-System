"use client";

import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import type { ActionState } from "@/lib/api/action-state";
import { initialActionState } from "@/lib/api/action-state";

type Options = {
  /** Called whenever state transitions to success, to e.g. close a dialog. */
  onSuccess?: () => void;
  /** Called whenever state transitions to error, to e.g. close a dialog. */
  onError?: () => void;
  /**
   * If true, skip the default error toast (useful when the UI shows an
   * inline alert instead, e.g. payment-decline reasons).
   */
  suppressErrorToast?: (state: ActionState) => boolean;
};

/**
 * Surfaces ActionState transitions as Sonner toasts and triggers a
 * router.refresh on success. Side effects on success/error are delegated
 * to optional callbacks (e.g. closing a controlled dialog) so the calling
 * component does not call setState() directly inside its own useEffect.
 */
export function useToastEffect(state: ActionState, options: Options = {}) {
  const router = useRouter();
  const last = useRef<ActionState>(initialActionState);
  const optsRef = useRef(options);

  useEffect(() => {
    optsRef.current = options;
  }, [options]);

  useEffect(() => {
    if (state === last.current) return;
    last.current = state;
    const o = optsRef.current;

    if (state.status === "success") {
      toast.success(state.message);
      o.onSuccess?.();
      router.refresh();
    } else if (state.status === "error") {
      const skip = o.suppressErrorToast?.(state);
      if (!skip) {
        const msg = state.message.toLowerCase();
        if (msg.includes("unpaid fine")) {
          toast.error(state.message, {
            action: {
              label: "Pay now",
              onClick: () => router.push("/my/fines"),
            },
          });
        } else {
          toast.error(state.message);
        }
      }
      o.onError?.();
    }
  }, [state, router]);
}
