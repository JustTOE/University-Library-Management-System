import Link from "next/link";

import { UserForm } from "@/components/admin/user-form";
import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function NewUserPage() {
  return (
    <div className="flex flex-col gap-4">
      <Link
        href="/admin/users"
        className={buttonVariants({ variant: "ghost", size: "sm" })}
      >
        ← Back to users
      </Link>
      <Card>
        <CardHeader>
          <CardTitle>New user</CardTitle>
        </CardHeader>
        <CardContent>
          <UserForm mode="create" />
        </CardContent>
      </Card>
    </div>
  );
}
