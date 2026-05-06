import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { EmptyState } from "@/components/common/empty-state";

export default function EditBookNotFound() {
  return (
    <EmptyState
      title="Book not found"
      description="It may have been deleted or never existed."
      action={
        <Link
          href="/librarian/catalog"
          className={buttonVariants({ variant: "outline" })}
        >
          Back to catalog
        </Link>
      }
    />
  );
}
