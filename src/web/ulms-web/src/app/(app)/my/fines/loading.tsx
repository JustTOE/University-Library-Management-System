import { Skeleton } from "@/components/ui/skeleton";

export default function MyFinesLoading() {
  return (
    <div className="flex flex-col gap-4">
      <Skeleton className="h-20 w-full" />
      <div className="rounded-lg border border-border">
        {Array.from({ length: 4 }).map((_, i) => (
          <div
            key={i}
            className="flex items-center gap-3 border-b border-border px-3 py-3 last:border-b-0"
          >
            <Skeleton className="h-5 flex-1" />
            <Skeleton className="hidden h-5 w-24 sm:block" />
            <Skeleton className="h-5 w-20" />
          </div>
        ))}
      </div>
    </div>
  );
}
