import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

type Status = "available" | "limited" | "out";

function statusOf(available: number, total: number): Status {
  if (available <= 0) return "out";
  if (available < total) return "limited";
  return "available";
}

const STYLES: Record<Status, { dot: string; badge: string }> = {
  available: {
    dot: "bg-success",
    badge: "bg-success/10 text-success dark:bg-success/15",
  },
  limited: {
    dot: "bg-warning",
    badge: "bg-warning/15 text-warning-foreground dark:bg-warning/20 dark:text-warning",
  },
  out: {
    dot: "bg-destructive",
    badge: "bg-destructive/10 text-destructive dark:bg-destructive/20",
  },
};

/**
 * Availability indicator with a colored status dot.
 * - `compact` (default): "{available} / {total}" — used in catalog lists.
 * - `verbose`: "{available} of {total} available" / "Unavailable" — used on detail pages.
 */
export function AvailabilityBadge({
  available,
  total,
  variant = "compact",
}: {
  available: number;
  total: number;
  variant?: "compact" | "verbose";
}) {
  const status = statusOf(available, total);
  const styles = STYLES[status];

  const label =
    variant === "verbose"
      ? status === "out"
        ? "Unavailable"
        : `${available} of ${total} available`
      : status === "out"
        ? `Out · 0 / ${total}`
        : `${available} / ${total}`;

  return (
    <Badge
      variant="outline"
      className={cn("border-transparent gap-1.5", styles.badge)}
    >
      <span
        className={cn("size-1.5 shrink-0 rounded-full", styles.dot)}
        aria-hidden
      />
      {label}
    </Badge>
  );
}
