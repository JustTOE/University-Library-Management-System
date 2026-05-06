import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";

export default function BookNotFound() {
  return (
    <div className="flex flex-col items-center gap-4 py-20 text-center">
      <h1 className="text-2xl font-semibold tracking-tight">Book not found</h1>
      <p className="text-sm text-muted-foreground">
        That book is no longer in the catalog.
      </p>
      <Link href="/catalog" className={buttonVariants()}>
        Back to catalog
      </Link>
    </div>
  );
}
