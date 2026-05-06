import { AlertCircle } from "lucide-react";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

export function ErrorAlert({
  title = "Something went wrong",
  message,
}: {
  title?: string;
  message?: string;
}) {
  return (
    <Alert variant="destructive">
      <AlertCircle className="size-4" aria-hidden />
      <AlertTitle>{title}</AlertTitle>
      {message ? <AlertDescription>{message}</AlertDescription> : null}
    </Alert>
  );
}
