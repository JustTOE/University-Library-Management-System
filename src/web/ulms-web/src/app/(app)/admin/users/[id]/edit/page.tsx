import Link from "next/link";
import { notFound } from "next/navigation";
import { getTranslations } from "next-intl/server";

import { UserForm } from "@/components/admin/user-form";
import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ApiError } from "@/lib/api/client";
import { getUserById, type UserResponse } from "@/lib/api/users";
import { readSession } from "@/lib/auth/session";

async function loadUser(userId: number, token: string | undefined): Promise<UserResponse> {
  try {
    return await getUserById(userId, { token });
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }
}

export default async function EditUserPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const userId = Number(id);
  if (!Number.isFinite(userId) || userId <= 0) {
    notFound();
  }

  const session = await readSession();
  const user = await loadUser(userId, session?.token);
  const t = await getTranslations("admin.users");

  return (
    <div className="flex flex-col gap-4">
      <Link
        href="/admin/users"
        className={buttonVariants({ variant: "ghost", size: "sm" })}
      >
        {t("backToUsers")}
      </Link>
      <Card>
        <CardHeader>
          <CardTitle>{t("editTitle")}</CardTitle>
        </CardHeader>
        <CardContent>
          <UserForm mode="edit" userId={userId} initial={user} />
        </CardContent>
      </Card>
    </div>
  );
}
