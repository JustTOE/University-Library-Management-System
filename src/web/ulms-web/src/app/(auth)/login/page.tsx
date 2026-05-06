import Link from "next/link";

import { LoginForm } from "@/components/auth/login-form";

type Props = {
  searchParams: Promise<{
    redirectTo?: string;
    reason?: string;
    registered?: string;
  }>;
};

export default async function LoginPage({ searchParams }: Props) {
  const params = await searchParams;
  const redirectTo =
    typeof params.redirectTo === "string" && params.redirectTo.startsWith("/")
      ? params.redirectTo
      : "/catalog";

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-lg font-semibold">Sign in</h2>
        <p className="text-sm text-muted-foreground">
          Use your university email and password.
        </p>
      </div>

      <LoginForm
        redirectTo={redirectTo}
        expiredNotice={params.reason === "expired"}
        registeredNotice={params.registered === "1"}
      />

      <p className="text-center text-sm text-muted-foreground">
        Don&apos;t have an account?{" "}
        <Link href="/register" className="text-foreground underline">
          Register
        </Link>
      </p>
    </div>
  );
}
