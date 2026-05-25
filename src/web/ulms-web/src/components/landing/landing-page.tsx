import Link from "next/link";
import { BookOpen, Bell, Clock, Library } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const FEATURES = [
  {
    icon: BookOpen,
    title: "Browse & borrow",
    description:
      "Search the catalog by title, author, or subject and borrow a book in a single click.",
  },
  {
    icon: Clock,
    title: "Track your loans",
    description:
      "Keep due dates, renewals, and reservations in one place — no more guesswork.",
  },
  {
    icon: Bell,
    title: "Stay notified",
    description:
      "Get reminders before a book is due and an alert the moment a reservation is ready.",
  },
];

export function LandingPage() {
  return (
    <main id="main" className="flex min-h-screen flex-col">
      <header className="border-b border-border">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4">
          <span className="flex items-center gap-2 font-semibold text-foreground">
            <Library className="size-5 text-primary" aria-hidden />
            ULMS
          </span>
          <div className="flex items-center gap-2">
            <Link
              href="/login"
              className={buttonVariants({ variant: "ghost", size: "sm" })}
            >
              Sign in
            </Link>
            <Link
              href="/register"
              className={buttonVariants({ size: "sm" })}
            >
              Register
            </Link>
          </div>
        </div>
      </header>

      <section className="bg-gradient-to-b from-accent/40 to-background">
        <div className="mx-auto flex max-w-3xl flex-col items-center gap-6 px-4 py-20 text-center sm:py-28">
          <span className="inline-flex items-center gap-2 rounded-full border border-border bg-card px-3 py-1 text-xs font-medium text-muted-foreground">
            <span className="size-1.5 rounded-full bg-primary" aria-hidden />
            University Library Management System
          </span>
          <h1 className="text-4xl font-semibold tracking-tight text-balance sm:text-5xl">
            Your university library, online.
          </h1>
          <p className="max-w-xl text-base text-muted-foreground text-balance sm:text-lg">
            Browse the catalog, borrow and renew books, and manage your
            reservations and fines — all from one place.
          </p>
          <div className="flex flex-col gap-3 sm:flex-row">
            <Link
              href="/login?redirectTo=%2Fcatalog"
              className={cn(buttonVariants({ size: "lg" }), "h-11 px-6 text-base")}
            >
              Browse the catalog
            </Link>
            <Link
              href="/register"
              className={cn(
                buttonVariants({ variant: "outline", size: "lg" }),
                "h-11 px-6 text-base",
              )}
            >
              Create an account
            </Link>
          </div>
        </div>
      </section>

      <section className="mx-auto w-full max-w-6xl px-4 py-16">
        <div className="grid gap-6 sm:grid-cols-3">
          {FEATURES.map(({ icon: Icon, title, description }) => (
            <Card key={title} className="h-full">
              <CardHeader>
                <span className="flex size-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                  <Icon className="size-5" aria-hidden />
                </span>
                <CardTitle className="text-base">{title}</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">{description}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      <footer className="mt-auto border-t border-border">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-6 text-sm text-muted-foreground">
          <span>ULMS — University Library Management System</span>
          <Link href="/login" className="hover:text-foreground hover:underline">
            Sign in
          </Link>
        </div>
      </footer>
    </main>
  );
}
