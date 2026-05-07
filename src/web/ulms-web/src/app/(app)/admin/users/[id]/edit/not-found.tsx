import Link from "next/link";
import { getTranslations } from "next-intl/server";

import { buttonVariants } from "@/components/ui/button";

export default async function EditUserNotFound() {
  const t = await getTranslations("admin.users");
  return (
    <div className="flex flex-col items-center gap-4 py-12 text-center">
      <h2 className="text-xl font-semibold">{t("userNotFoundTitle")}</h2>
      <p className="text-sm text-muted-foreground">
        {t("userNotFoundDescription")}
      </p>
      <Link href="/admin/users" className={buttonVariants()}>
        {t("backToUsers")}
      </Link>
    </div>
  );
}
