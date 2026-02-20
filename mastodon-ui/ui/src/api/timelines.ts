/**
 * Timeline API endpoints
 */

import { client, API_BASE } from './client';
import type { Status, PagedResponse, PaginationParams } from '@/types';

const TIMELINES_BASE = `${API_BASE}/timelines`;

export const timelinesApi = {
  // Get home timeline
  home: (params?: PaginationParams): Promise<PagedResponse<Status>> =>
    client.paged<Status>(`${TIMELINES_BASE}/home`, params),

  // Get public timeline
  public: (params?: PaginationParams & { local?: boolean; remote?: boolean; only_media?: boolean }): Promise<PagedResponse<Status>> =>
    client.paged<Status>(`${TIMELINES_BASE}/public`, params),

  // Get hashtag timeline
  hashtag: (hashtag: string, params?: PaginationParams & { local?: boolean; remote?: boolean; only_media?: boolean }): Promise<PagedResponse<Status>> =>
    client.paged<Status>(`${TIMELINES_BASE}/tag/${hashtag}`, params),

  // Get list timeline
  list: (listId: string, params?: PaginationParams): Promise<PagedResponse<Status>> =>
    client.paged<Status>(`${TIMELINES_BASE}/list/${listId}`, params),

  // Get direct messages timeline (conversations)
  direct: (params?: PaginationParams): Promise<PagedResponse<Status>> =>
    client.paged<Status>(`${API_BASE}/conversations`, params),
};
