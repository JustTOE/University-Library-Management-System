import { ErrorAlert } from "@/components/common/error-alert";
import { EmptyState } from "@/components/common/empty-state";
import { NotificationStatusBadge } from "@/components/common/status-pill";
import { AcknowledgeNotificationButton } from "@/components/my/acknowledge-notification-button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { ApiError } from "@/lib/api/client";
import {
  listNotificationsByUser,
  type NotificationResponse,
} from "@/lib/api/notifications";
import { readSession } from "@/lib/auth/session";
import { formatDateTime } from "@/lib/format";

const TYPE_LABEL: Record<
  NonNullable<NotificationResponse["type"]>,
  string
> = {
  DUE_REMINDER: "Due reminder",
  OVERDUE_ALERT: "Overdue",
  RESERVATION_READY: "Reservation ready",
  GENERAL: "General",
};

export default async function MyNotificationsPage() {
  const session = await readSession();
  if (!session?.user.id) {
    return <ErrorAlert message="Could not load your notifications." />;
  }

  let notifications: NotificationResponse[] = [];
  let errorMessage: string | null = null;
  try {
    notifications = await listNotificationsByUser(session.user.id, {
      token: session.token,
    });
  } catch (error) {
    errorMessage =
      error instanceof ApiError
        ? error.message || "Could not load your notifications."
        : "Could not load your notifications.";
  }

  if (errorMessage) return <ErrorAlert message={errorMessage} />;

  if (notifications.length === 0) {
    return (
      <EmptyState
        title="No notifications"
        description="Reservation-ready alerts and due reminders will land here."
      />
    );
  }

  // Newest first by sentDate.
  const sorted = [...notifications].sort((a, b) => {
    const ad = a.sentDate ?? "";
    const bd = b.sentDate ?? "";
    return ad < bd ? 1 : ad > bd ? -1 : 0;
  });

  return (
    <div className="flex flex-col gap-3">
      {sorted.map((notification) => (
        <NotificationCard
          key={notification.id}
          notification={notification}
        />
      ))}
    </div>
  );
}

function NotificationCard({
  notification,
}: {
  notification: NotificationResponse;
}) {
  const typeLabel = notification.type
    ? TYPE_LABEL[notification.type]
    : "General";
  const isUnread = notification.status === "NOT_ACKNOWLEDGED";
  return (
    <Card data-unread={isUnread}>
      <CardContent className="flex flex-col gap-2 px-4 py-3">
        <div className="flex flex-wrap items-center gap-2">
          <NotificationStatusBadge status={notification.status} />
          <Badge variant="outline">{typeLabel}</Badge>
          <span className="text-xs text-muted-foreground">
            {formatDateTime(notification.sentDate)}
          </span>
          {isUnread && notification.id ? (
            <div className="ml-auto">
              <AcknowledgeNotificationButton
                notificationId={notification.id}
              />
            </div>
          ) : null}
        </div>
        <p className="text-sm text-foreground">
          {notification.message ?? "(No message)"}
        </p>
      </CardContent>
    </Card>
  );
}
