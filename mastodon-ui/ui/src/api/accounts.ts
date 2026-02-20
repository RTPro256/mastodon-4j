/**
 * Account API endpoints
 */

import { client, API_BASE } from './client';
import type { Account, Relationship, Status, PagedResponse, PaginationParams, AccountUpdateRequest } from '@/types';

const ACCOUNTS_BASE = `${API_BASE}/accounts`;

export const accountsApi = {
  // Get account by ID
  get: (id: string): Promise<Account> =>
    client.get<Account>(`${ACCOUNTS_BASE}/${id}`),

  // Get current user's account
  verifyCredentials: (): Promise<Account> =>
    client.get<Account>(`${ACCOUNTS_BASE}/verify_credentials`),

  // Update current user's account
  updateCredentials: (data: AccountUpdateRequest): Promise<Account> =>
    client.patch<Account>(`${ACCOUNTS_BASE}/update_credentials`, data),

  // Get account's statuses
  statuses: (id: string, params?: PaginationParams & {
    only_media?: boolean;
    exclude_replies?: boolean;
    exclude_reblogs?: boolean;
    pinned?: boolean;
  }): Promise<PagedResponse<Status>> =>
    client.paged<Status>(`${ACCOUNTS_BASE}/${id}/statuses`, params),

  // Get account's followers
  followers: (id: string, params?: PaginationParams): Promise<PagedResponse<Account>> =>
    client.paged<Account>(`${ACCOUNTS_BASE}/${id}/followers`, params),

  // Get accounts that account is following
  following: (id: string, params?: PaginationParams): Promise<PagedResponse<Account>> =>
    client.paged<Account>(`${ACCOUNTS_BASE}/${id}/following`, params),

  // Follow an account
  follow: (id: string, params?: { reblogs?: boolean; notify?: boolean }): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/follow`, params),

  // Unfollow an account
  unfollow: (id: string): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/unfollow`),

  // Block an account
  block: (id: string): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/block`),

  // Unblock an account
  unblock: (id: string): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/unblock`),

  // Mute an account
  mute: (id: string, params?: { notifications?: boolean; duration?: number }): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/mute`, params),

  // Unmute an account
  unmute: (id: string): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/unmute`),

  // Pin account to profile
  pin: (id: string): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/pin`),

  // Unpin account from profile
  unpin: (id: string): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/unpin`),

  // Set note for account
  note: (id: string, comment: string): Promise<Relationship> =>
    client.post<Relationship>(`${ACCOUNTS_BASE}/${id}/note`, { comment }),

  // Get relationships for multiple accounts
  relationships: (ids: string[]): Promise<Relationship[]> => {
    const params = new URLSearchParams();
    ids.forEach(id => params.append('id[]', id));
    return fetch(`${ACCOUNTS_BASE}/relationships?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    }).then(res => res.json());
  },

  // Search for accounts
  search: (q: string, params?: { limit?: number; offset?: number; following?: boolean }): Promise<Account[]> =>
    client.get<Account[]>(`${API_BASE}/accounts/search`, { q, ...params }),

  // Lookup account by acct
  lookup: (acct: string): Promise<Account> =>
    client.get<Account>(`${ACCOUNTS_BASE}/lookup`, { acct }),
};
