/**
 * Notification API endpoints
 */

import { client, API_BASE } from './client';
import type { Notification, PagedResponse, PaginationParams, NotificationType } from '@/types';

const NOTIFICATIONS_BASE = `${API_BASE}/notifications`;

export const notificationsApi = {
  // Get all notifications
  list: (params?: PaginationParams & {
    types?: NotificationType[];
    exclude_types?: NotificationType[];
    account_id?: string;
  }): Promise<PagedResponse<Notification>> =>
    client.paged<Notification>(NOTIFICATIONS_BASE, params),

  // Get a single notification
  get: (id: string): Promise<Notification> =>
    client.get<Notification>(`${NOTIFICATIONS_BASE}/${id}`),

  // Clear all notifications
  clear: (): Promise<void> =>
    client.post<void>(`${NOTIFICATIONS_BASE}/clear`),

  // Dismiss a single notification
  dismiss: (id: string): Promise<void> =>
    client.post<void>(`${NOTIFICATIONS_BASE}/${id}/dismiss`),

  // Mark notifications as read (up to a specific ID)
  markAsRead: (id?: string): Promise<void> =>
    client.post<void>(`${NOTIFICATIONS_BASE}/mark_as_read`, { id }),
};
