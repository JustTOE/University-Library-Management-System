"use client";

import { logoutAction } from "@/lib/auth/actions";

/**
 * Renders a tiny form whose submit button is the actual menu item. Server
 * Actions can only run from forms (not bare onClick), so the form wrapping
 * is load-bearing.
 */
export function LogoutFormButton({ label = "Log out" }: { label?: string }) {
  return (
    <form action={logoutAction} className="contents">
      <button
        type="submit"
        className="group/dropdown-menu-item relative flex w-full cursor-default items-center gap-1.5 rounded-md px-1.5 py-1 text-left text-sm outline-hidden select-none focus:bg-accent focus:text-accent-foreground hover:bg-accent hover:text-accent-foreground"
      >
        {label}
      </button>
    </form>
  );
}
