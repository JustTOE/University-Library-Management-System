import Link from "next/link";
import { Bell, Library, User as UserIcon } from "lucide-react";

import {
  Avatar,
  AvatarFallback,
} from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { LogoutFormButton } from "@/components/auth/logout-button";
import type { UserResponse } from "@/lib/api/auth";
import { getUnreadCount } from "@/lib/api/notifications";

function initials(name?: string) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/).slice(0, 2);
  return parts.map((p) => p[0]?.toUpperCase() ?? "").join("") || "?";
}

async function fetchUnreadCount(
  user: UserResponse,
  token: string,
): Promise<number> {
  if (user.role !== "STUDENT" || user.id == null) return 0;
  try {
    const result = await getUnreadCount(user.id, { token });
    return result.count ?? 0;
  } catch {
    return 0;
  }
}

export async function Navbar({
  user,
  token,
}: {
  user: UserResponse;
  token: string;
}) {
  const unread = await fetchUnreadCount(user, token);
  const isStudent = user.role === "STUDENT";
  const isLibrarian = user.role === "LIBRARIAN" || user.role === "ADMIN";
  const isAdmin = user.role === "ADMIN";

  return (
    <header className="border-b border-border bg-background">
      <nav className="mx-auto flex h-14 max-w-6xl items-center gap-4 px-4">
        <Link
          href="/catalog"
          className="flex items-center gap-2 font-semibold text-foreground"
        >
          <Library className="size-5" aria-hidden />
          ULMS
        </Link>

        <ul className="flex flex-1 items-center gap-1">
          <li>
            <Link
              href="/catalog"
              className="rounded-md px-3 py-1.5 text-sm text-foreground hover:bg-muted"
            >
              Catalog
            </Link>
          </li>
        </ul>

        {isLibrarian ? (
          <DropdownMenu>
            <DropdownMenuTrigger
              render={
                <Button variant="ghost" size="default">
                  Librarian
                </Button>
              }
            />
            <DropdownMenuContent align="end">
              <DropdownMenuItem render={<Link href="/librarian/catalog" />}>
                Catalog
              </DropdownMenuItem>
              <DropdownMenuItem render={<Link href="/librarian/returns" />}>
                Returns
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null}

        {isAdmin ? (
          <DropdownMenu>
            <DropdownMenuTrigger
              render={
                <Button variant="ghost" size="default">
                  Admin
                </Button>
              }
            />
            <DropdownMenuContent align="end">
              <DropdownMenuItem render={<Link href="/admin/users" />}>
                Users
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null}

        {isStudent ? (
          <Link
            href="/my/notifications"
            aria-label={
              unread > 0
                ? `Notifications: ${unread} unread`
                : "Notifications"
            }
            className="relative inline-flex h-8 items-center justify-center rounded-md px-2 text-foreground hover:bg-muted"
          >
            <Bell className="size-4" aria-hidden />
            {unread > 0 ? (
              <Badge
                variant="destructive"
                className="absolute -top-1.5 -right-1 h-5 min-w-5 rounded-full px-1 text-[10px]"
              >
                {unread > 99 ? "99+" : unread}
              </Badge>
            ) : null}
          </Link>
        ) : null}

        {isStudent ? (
          <DropdownMenu>
            <DropdownMenuTrigger
              render={
                <Button variant="ghost" size="default">
                  My library
                </Button>
              }
            />
            <DropdownMenuContent align="end">
              <DropdownMenuItem render={<Link href="/my/loans" />}>
                Loans
              </DropdownMenuItem>
              <DropdownMenuItem render={<Link href="/my/reservations" />}>
                Reservations
              </DropdownMenuItem>
              <DropdownMenuItem render={<Link href="/my/fines" />}>
                Fines
              </DropdownMenuItem>
              <DropdownMenuItem render={<Link href="/my/notifications" />}>
                Notifications
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null}

        <DropdownMenu>
          <DropdownMenuTrigger
            className="flex items-center gap-2 rounded-full focus:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            aria-label="Account menu"
          >
            <Avatar className="size-8">
              <AvatarFallback>
                {user.name ? initials(user.name) : <UserIcon className="size-4" aria-hidden />}
              </AvatarFallback>
            </Avatar>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <div className="flex flex-col gap-0.5 px-2 py-1.5">
              <span className="text-sm font-medium text-foreground">
                {user.name ?? user.email}
              </span>
              <span className="text-xs text-muted-foreground">{user.email}</span>
              {user.role && (
                <span className="text-[10px] uppercase tracking-wider text-muted-foreground">
                  {user.role}
                </span>
              )}
            </div>
            <DropdownMenuSeparator />
            <LogoutFormButton />
          </DropdownMenuContent>
        </DropdownMenu>
      </nav>
    </header>
  );
}
