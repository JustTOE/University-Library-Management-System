import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";

export default function EditUserNotFound() {
  return (
    <div className="flex flex-col items-center gap-4 py-12 text-center">
      <h2 className="text-xl font-semibold">User not found</h2>
      <p className="text-sm text-muted-foreground">
        This user no longer exists or has been deleted.
      </p>
      <Link href="/admin/users" className={buttonVariants()}>
        Back to users
      </Link>
    </div>
  );
}
