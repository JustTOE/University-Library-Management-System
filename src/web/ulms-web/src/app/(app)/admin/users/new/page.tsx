import Link from "next/link";
import { getTranslations } from "next-intl/server";

import { UserForm } from "@/components/admin/user-form";
import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default async function NewUserPage() {
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
          <CardTitle>{t("createTitle")}</CardTitle>
        </CardHeader>
        <CardContent>
          <UserForm mode="create" />
        </CardContent>
      </Card>
    </div>
  );
}
