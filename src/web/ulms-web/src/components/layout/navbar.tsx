import Link from "next/link";
import { Library, User as UserIcon } from "lucide-react";

import {
  Avatar,
  AvatarFallback,
} from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { LogoutFormButton } from "@/components/auth/logout-button";
import type { UserResponse } from "@/lib/api/auth";

function initials(name?: string) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/).slice(0, 2);
  return parts.map((p) => p[0]?.toUpperCase() ?? "").join("") || "?";
}

export function Navbar({ user }: { user: UserResponse }) {
  return (
    <header className="border-b border-border bg-background">
      <nav className="mx-auto flex h-14 max-w-6xl items-center gap-6 px-4">
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
            <DropdownMenuLabel className="flex flex-col gap-0.5 px-2 py-1.5">
              <span className="text-sm font-medium text-foreground">
                {user.name ?? user.email}
              </span>
              <span className="text-xs text-muted-foreground">{user.email}</span>
              {user.role && (
                <span className="text-[10px] uppercase tracking-wider text-muted-foreground">
                  {user.role}
                </span>
              )}
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <LogoutFormButton />
          </DropdownMenuContent>
        </DropdownMenu>
      </nav>
    </header>
  );
}
