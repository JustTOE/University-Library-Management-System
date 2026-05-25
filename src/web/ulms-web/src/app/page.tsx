import { redirect } from "next/navigation";

import { LandingPage } from "@/components/landing/landing-page";
import { readSession } from "@/lib/auth/session";

export default async function HomePage() {
  const session = await readSession();
  if (session) redirect("/catalog");
  return <LandingPage />;
}
