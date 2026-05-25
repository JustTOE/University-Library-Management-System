import Link from "next/link";
import { redirect } from "next/navigation";
import { Library } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { readSession } from "@/lib/auth/session";

export default async function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const session = await readSession();
  if (session) redirect("/catalog");

  return (
    <main
      id="main"
      className="flex min-h-screen items-center justify-center bg-gradient-to-b from-accent/40 to-muted px-4 py-8"
    >
      <Card className="w-full max-w-md">
        <CardHeader>
          <Link
            href="/"
            className="flex items-center gap-2 text-muted-foreground hover:text-foreground"
          >
            <Library className="size-5 text-primary" aria-hidden />
            <CardTitle className="text-xl text-foreground">ULMS</CardTitle>
          </Link>
        </CardHeader>
        <CardContent>{children}</CardContent>
      </Card>
    </main>
  );
}
