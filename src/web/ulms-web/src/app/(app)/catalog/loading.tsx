import { Skeleton } from "@/components/ui/skeleton";

export default function CatalogLoading() {
  return (
    <div className="flex flex-col gap-6">
      <Skeleton className="h-8 w-32" />
      <Skeleton className="h-32 w-full" />
      <div className="rounded-lg border border-border">
        {Array.from({ length: 8 }).map((_, i) => (
          <div
            key={i}
            className="flex items-center gap-3 border-b border-border px-3 py-3 last:border-b-0"
          >
            <Skeleton className="h-5 flex-1" />
            <Skeleton className="hidden h-5 w-32 sm:block" />
            <Skeleton className="h-5 w-16" />
          </div>
        ))}
      </div>
    </div>
  );
}
