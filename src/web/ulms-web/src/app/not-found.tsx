import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";

export default function NotFound() {
  return (
    <main className="flex min-h-screen items-center justify-center px-4 py-8">
      <div className="flex flex-col items-center gap-4 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Page not found</h1>
        <p className="text-sm text-muted-foreground">
          The page you were looking for doesn&apos;t exist.
        </p>
        <Link href="/" className={buttonVariants()}>
          Go home
        </Link>
      </div>
    </main>
  );
}
