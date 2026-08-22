export interface Notification {
  id: number;
  type: string;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface NotificationPreference {
  type: string;
  emailEnabled: boolean;
  inAppEnabled: boolean;
}
