import { Skeleton } from "@/components/ui/skeleton";

export default function BookDetailLoading() {
  return (
    <div className="flex flex-col gap-6">
      <Skeleton className="h-7 w-32" />
      <Skeleton className="h-48 w-full" />
      <Skeleton className="h-9 w-32" />
    </div>
  );
}
