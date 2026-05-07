import Link from "next/link";
import { getTranslations } from "next-intl/server";

import { CatalogPagination } from "@/components/catalog/catalog-pagination";
import { UserSearchForm } from "@/components/admin/user-search-form";
import { UserTable } from "@/components/admin/user-table";
import { ErrorAlert } from "@/components/common/error-alert";
import { buttonVariants } from "@/components/ui/button";
import { ApiError } from "@/lib/api/client";
import { listUsers, type UserResponse } from "@/lib/api/users";
import { readSession } from "@/lib/auth/session";

const DEFAULT_SIZE = 20;

function pickString(
  v: string | string[] | undefined,
): string | undefined {
  if (Array.isArray(v)) return v[0];
  return v;
}

function pickInt(
  v: string | string[] | undefined,
  fallback: number,
): number {
  const value = pickString(v);
  if (!value) return fallback;
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
}

function matchesQuery(user: UserResponse, q: string): boolean {
  const haystack = [user.name, user.email, user.universityId, user.staffId]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
  return haystack.includes(q);
}

export default async function AdminUsersPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const q = pickString(params.q)?.trim() ?? "";
  const role = pickString(params.role) ?? "";
  const page = pickInt(params.page, 0);
  const size = pickInt(params.size, DEFAULT_SIZE);

  const session = await readSession();
  const token = session?.token;
  const t = await getTranslations("admin.users");
  const tErr = await getTranslations("errors");

  let allUsers: UserResponse[] = [];
  let errorMessage: string | null = null;

  try {
    allUsers = await listUsers({ token });
  } catch (error) {
    errorMessage =
      error instanceof ApiError
        ? error.message || tErr("couldNotLoadUsers")
        : tErr("couldNotLoadUsers");
  }

  const qLower = q.toLowerCase();
  const filtered = allUsers
    .filter((u) => (role ? u.role === role : true))
    .filter((u) => (qLower ? matchesQuery(u, qLower) : true))
    .sort((a, b) => (a.name ?? "").localeCompare(b.name ?? ""));

  const totalElements = filtered.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const safePage = Math.min(page, totalPages - 1);
  const slice = filtered.slice(
    safePage * size,
    safePage * size + size,
  );

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between gap-2">
        <div className="flex flex-col gap-1">
          <h2 className="text-xl font-semibold tracking-tight">
            {t("title")}
          </h2>
          <p className="text-sm text-muted-foreground">{t("subtitle")}</p>
        </div>
        <Link
          href="/admin/users/new"
          className={buttonVariants()}
          data-testid="new-user"
        >
          {t("newUser")}
        </Link>
      </div>

      <UserSearchForm defaults={{ q, role }} action="/admin/users" />

      {errorMessage ? (
        <ErrorAlert message={errorMessage} />
      ) : (
        <>
          <UserTable users={slice} />
          <CatalogPagination
            number={safePage}
            totalPages={totalPages}
            totalElements={totalElements}
            baseQuery={{
              q: q || undefined,
              role: role || undefined,
              size: size === DEFAULT_SIZE ? undefined : String(size),
            }}
          />
        </>
      )}
    </div>
  );
}
