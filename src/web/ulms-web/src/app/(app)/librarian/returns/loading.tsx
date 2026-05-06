import { Skeleton } from "@/components/ui/skeleton";

export default function LibrarianReturnsLoading() {
  return (
    <div className="flex flex-col gap-6">
      <Skeleton className="h-7 w-40" />
      <Skeleton className="h-32 w-full" />
      <div className="rounded-xl border border-border p-4">
        <Skeleton className="mb-3 h-5 w-48" />
        <Skeleton className="mb-3 h-4 w-32" />
        <Skeleton className="h-24 w-full" />
      </div>
    </div>
  );
}
