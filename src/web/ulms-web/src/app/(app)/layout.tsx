import { headers } from "next/headers";

import { Navbar } from "@/components/layout/navbar";
import { Toaster } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { requireAuth } from "@/lib/auth/guards";

export default async function AppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  // The proxy strips /login/etc. but page paths are best derived from
  // headers so requireAuth's redirectTo points at the *requested* URL.
  const hdrs = await headers();
  const path = hdrs.get("x-current-path") ?? "/catalog";
  const { user } = await requireAuth(path);

  return (
    <TooltipProvider>
      <div className="flex min-h-screen flex-col bg-background">
        <Navbar user={user} />
        <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
          {children}
        </main>
        <Toaster richColors closeButton position="top-right" />
      </div>
    </TooltipProvider>
  );
}
