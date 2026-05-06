import {
  differenceInCalendarDays,
  format,
  formatDistanceToNowStrict,
  parseISO,
} from "date-fns";

const MONEY = new Intl.NumberFormat("en-IE", {
  style: "currency",
  currency: "EUR",
});

function toDate(iso?: string | null): Date | null {
  if (!iso) return null;
  const parsed = parseISO(iso);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

export function formatDateOnly(iso?: string | null): string {
  const d = toDate(iso);
  return d ? format(d, "EEE, d MMM yyyy") : "—";
}

export function formatDateTime(iso?: string | null): string {
  const d = toDate(iso);
  return d ? format(d, "d MMM yyyy, HH:mm") : "—";
}

export function formatRelative(iso?: string | null): string {
  const d = toDate(iso);
  if (!d) return "—";
  const now = Date.now();
  const distance = formatDistanceToNowStrict(d);
  return d.getTime() >= now ? `in ${distance}` : `${distance} ago`;
}

export function formatMoney(amount?: number | null): string {
  if (amount == null || Number.isNaN(amount)) return "—";
  return MONEY.format(amount);
}

export function daysUntil(iso?: string | null): number | null {
  const d = toDate(iso);
  if (!d) return null;
  return differenceInCalendarDays(d, new Date());
}
