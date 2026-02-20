/**
 * Status API endpoints
 */

import { client, API_BASE } from './client';
import type { Status, StatusCreateRequest, Context } from '@/types';

const STATUSES_BASE = `${API_BASE}/statuses`;

export const statusesApi = {
  // Create a new status
  create: (data: StatusCreateRequest): Promise<Status> =>
    client.post<Status>(STATUSES_BASE, data),

  // Get a status by ID
  get: (id: string): Promise<Status> =>
    client.get<Status>(`${STATUSES_BASE}/${id}`),

  // Delete a status
  delete: (id: string): Promise<void> =>
    client.delete<void>(`${STATUSES_BASE}/${id}`),

  // Get status context (ancestors/descendants)
  context: (id: string): Promise<Context> =>
    client.get<Context>(`${STATUSES_BASE}/${id}/context`),

  // Favourite a status
  favourite: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/favourite`),

  // Unfavourite a status
  unfavourite: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/unfavourite`),

  // Reblog (boost) a status
  reblog: (id: string, visibility?: 'public' | 'unlisted'): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/reblog`, { visibility }),

  // Unreblog a status
  unreblog: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/unreblog`),

  // Bookmark a status
  bookmark: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/bookmark`),

  // Unbookmark a status
  unbookmark: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/unbookmark`),

  // Mute conversation
  mute: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/mute`),

  // Unmute conversation
  unmute: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/unmute`),

  // Pin status to profile
  pin: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/pin`),

  // Unpin status from profile
  unpin: (id: string): Promise<Status> =>
    client.post<Status>(`${STATUSES_BASE}/${id}/unpin`),

  // Edit a status
  edit: (id: string, data: StatusCreateRequest): Promise<Status> =>
    client.put<Status>(`${STATUSES_BASE}/${id}`, data),

  // Get status source (for editing)
  source: (id: string): Promise<{ id: string; text: string; spoiler_text: string }> =>
    client.get(`${STATUSES_BASE}/${id}/source`),

  // Get who favourited a status
  favouritedBy: (id: string): Promise<Status[]> =>
    client.get<Status[]>(`${STATUSES_BASE}/${id}/favourited_by`),

  // Get who reblogged a status
  rebloggedBy: (id: string): Promise<Status[]> =>
    client.get<Status[]>(`${STATUSES_BASE}/${id}/reblogged_by`),

  // Translate a status
  translate: (id: string, lang?: string): Promise<{ content: string; language: string; provider: string }> =>
    client.post(`${STATUSES_BASE}/${id}/translate`, { lang }),
};
