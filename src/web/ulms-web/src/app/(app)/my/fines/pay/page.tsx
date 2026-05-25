import Link from "next/link";

import { CheckoutForm } from "@/components/my/checkout-form";
import { EmptyState } from "@/components/common/empty-state";
import { ErrorAlert } from "@/components/common/error-alert";
import { buttonVariants } from "@/components/ui/button";
import { ApiError } from "@/lib/api/client";
import { listUnpaidFinesByUser, type FineResponse } from "@/lib/api/fines";
import { readSession } from "@/lib/auth/session";

export default async function PayFinesPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const session = await readSession();
  if (!session?.user.id) {
    return <ErrorAlert message="Could not load your fines." />;
  }

  const userId = session.user.id;
  const token = session.token;
  const preselectId = parseFineId((await searchParams).fineId);

  let fines: FineResponse[] = [];
  let errorMessage: string | null = null;

  try {
    fines = await listUnpaidFinesByUser(userId, { token });
  } catch (error) {
    errorMessage =
      error instanceof ApiError
        ? error.message || "Could not load your fines."
        : "Could not load your fines.";
  }

  if (errorMessage) return <ErrorAlert message={errorMessage} />;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold tracking-tight">Pay online</h1>
        <p className="text-sm text-muted-foreground">
          Pay your outstanding fines by card.
        </p>
      </div>

      {fines.length === 0 ? (
        <EmptyState
          title="Nothing to pay"
          description="You have no unpaid fines."
          action={
            <Link href="/my/fines" className={buttonVariants({ variant: "outline" })}>
              Back to fines
            </Link>
          }
        />
      ) : (
        <CheckoutForm fines={fines} preselectId={preselectId} />
      )}
    </div>
  );
}

function parseFineId(raw: string | string[] | undefined): number | undefined {
  const value = Array.isArray(raw) ? raw[0] : raw;
  if (!value) return undefined;
  const n = Number(value);
  return Number.isInteger(n) && n > 0 ? n : undefined;
}
