"use client";

import { useLocale, useTranslations } from "next-intl";

import { setLocaleAction } from "@/i18n/actions";
import { LOCALES, type Locale } from "@/i18n/config";
import { cn } from "@/lib/utils";

export function LocaleSwitcher() {
  const current = useLocale() as Locale;
  const t = useTranslations("locale");

  return (
    <div
      role="group"
      aria-label={t("switchLabel")}
      className="inline-flex items-center gap-0.5 rounded-md border border-border bg-card p-0.5 text-xs"
    >
      {LOCALES.map((locale) => {
        const isActive = locale === current;
        const label =
          locale === "en" ? t("switchToEnglish") : t("switchToRomanian");
        return (
          <form key={locale} action={setLocaleAction}>
            <input type="hidden" name="locale" value={locale} />
            <button
              type="submit"
              aria-label={label}
              aria-pressed={isActive}
              className={cn(
                "rounded px-2 py-1 font-medium uppercase tracking-wider transition-colors",
                isActive
                  ? "bg-muted text-foreground"
                  : "text-muted-foreground hover:bg-muted hover:text-foreground",
              )}
            >
              {locale}
            </button>
          </form>
        );
      })}
    </div>
  );
}
