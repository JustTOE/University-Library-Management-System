import Link from "next/link";
import { notFound } from "next/navigation";
import { getTranslations } from "next-intl/server";

import { ActiveToggleButton } from "@/components/admin/active-toggle-button";
import { DeleteUserButton } from "@/components/admin/delete-user-button";
import { UserHistoryFines } from "@/components/admin/user-history-fines";
import { UserHistoryLoans } from "@/components/admin/user-history-loans";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/components/ui/tabs";
import { ApiError } from "@/lib/api/client";
import {
  getUserById,
  getUserHistory,
  type UserHistoryResponse,
  type UserResponse,
} from "@/lib/api/users";
import { readSession } from "@/lib/auth/session";

function RoleBadge({
  role,
  labels,
}: {
  role?: string;
  labels: { admin: string; librarian: string; student: string };
}) {
  switch (role) {
    case "ADMIN":
      return <Badge variant="default">{labels.admin}</Badge>;
    case "LIBRARIAN":
      return <Badge variant="secondary">{labels.librarian}</Badge>;
    case "STUDENT":
      return <Badge variant="outline">{labels.student}</Badge>;
    default:
      return <Badge variant="outline">—</Badge>;
  }
}

async function loadUser(
  userId: number,
  token: string | undefined,
): Promise<UserResponse> {
  try {
    return await getUserById(userId, { token });
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }
}

async function loadHistory(
  userId: number,
  token: string | undefined,
): Promise<UserHistoryResponse> {
  try {
    return await getUserHistory(userId, { token });
  } catch {
    return { loans: [], fines: [] };
  }
}

export default async function UserDetailPage({
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
  const token = session?.token;

  const [user, history, t, tCommon, tFields] = await Promise.all([
    loadUser(userId, token),
    loadHistory(userId, token),
    getTranslations("admin.users"),
    getTranslations("common"),
    getTranslations("admin.users.fields"),
  ]);

  const isActive = Boolean(user.isActive);
  const roleLabels = {
    admin: t("roleAdmin"),
    librarian: t("roleLibrarian"),
    student: t("roleStudent"),
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <Link
          href="/admin/users"
          className={buttonVariants({ variant: "ghost", size: "sm" })}
        >
          {t("backToUsers")}
        </Link>
        <div className="flex items-center gap-2">
          <Link
            href={`/admin/users/${userId}/edit`}
            className={buttonVariants({ variant: "outline", size: "sm" })}
          >
            {tCommon("edit")}
          </Link>
          <ActiveToggleButton
            userId={userId}
            name={user.name ?? "this user"}
            isActive={isActive}
          />
          <DeleteUserButton
            userId={userId}
            name={user.name ?? "this user"}
          />
        </div>
      </div>

      <Tabs defaultValue="profile" className="gap-4">
        <TabsList>
          <TabsTrigger value="profile">{t("tabProfile")}</TabsTrigger>
          <TabsTrigger value="loans">{t("tabLoans")}</TabsTrigger>
          <TabsTrigger value="fines">{t("tabFines")}</TabsTrigger>
        </TabsList>

        <TabsContent value="profile">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                {user.name ?? "—"}
                <RoleBadge role={user.role} labels={roleLabels} />
                {!isActive ? (
                  <Badge variant="outline" className="text-muted-foreground">
                    {t("inactive")}
                  </Badge>
                ) : null}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <dl className="grid gap-3 sm:grid-cols-2 text-sm">
                <div>
                  <dt className="text-xs uppercase tracking-wider text-muted-foreground">
                    {tFields("email")}
                  </dt>
                  <dd className="font-medium">{user.email ?? "—"}</dd>
                </div>
                <div>
                  <dt className="text-xs uppercase tracking-wider text-muted-foreground">
                    {tFields("universityId")}
                  </dt>
                  <dd className="font-medium">{user.universityId ?? "—"}</dd>
                </div>
                <div>
                  <dt className="text-xs uppercase tracking-wider text-muted-foreground">
                    {tFields("staffId")}
                  </dt>
                  <dd className="font-medium">{user.staffId ?? "—"}</dd>
                </div>
                <div>
                  <dt className="text-xs uppercase tracking-wider text-muted-foreground">
                    {tFields("phoneOptional")}
                  </dt>
                  <dd className="font-medium">{user.phone ?? "—"}</dd>
                </div>
              </dl>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="loans">
          <UserHistoryLoans loans={history.loans ?? []} />
        </TabsContent>

        <TabsContent value="fines">
          <UserHistoryFines fines={history.fines ?? []} />
        </TabsContent>
      </Tabs>
    </div>
  );
}
