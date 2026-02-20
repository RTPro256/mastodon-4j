/**
 * OAuth and instance API endpoints
 */

import { client, API_BASE, OAUTH_BASE } from './client';
import type { OAuthApp, TokenResponse, InstanceInfo, SearchResults, Preferences } from '@/types';

export const oauthApi = {
  // Create an OAuth application
  createApp: (data: {
    client_name: string;
    redirect_uris: string;
    scopes?: string;
    website?: string;
  }): Promise<OAuthApp> =>
    client.post<OAuthApp>(`${API_BASE}/apps`, data),

  // Get OAuth token via authorization code
  getToken: (data: {
    client_id: string;
    client_secret: string;
    redirect_uri: string;
    code: string;
    grant_type: 'authorization_code';
  }): Promise<TokenResponse> =>
    client.post<TokenResponse>(`${OAUTH_BASE}/token`, data),

  // Get OAuth token via password (for testing)
  getTokenWithPassword: (data: {
    client_id: string;
    client_secret: string;
    username: string;
    password: string;
    scope: string;
    grant_type: 'password';
  }): Promise<TokenResponse> =>
    client.post<TokenResponse>(`${OAUTH_BASE}/token`, data),

  // Revoke OAuth token
  revokeToken: (data: {
    client_id: string;
    client_secret: string;
    token: string;
  }): Promise<void> =>
    client.post<void>(`${OAUTH_BASE}/revoke`, data),

  // Generate OAuth authorization URL
  getAuthorizationUrl: (clientId: string, redirectUri: string, scope: string, state?: string): string => {
    const params = new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri,
      response_type: 'code',
      scope: scope,
    });
    if (state) {
      params.append('state', state);
    }
    return `${OAUTH_BASE}/authorize?${params.toString()}`;
  },
};

export const instanceApi = {
  // Get instance information
  get: (): Promise<InstanceInfo> =>
    client.get<InstanceInfo>(`${API_BASE}/instance`),

  // Get instance peers (domains this instance knows about)
  peers: (): Promise<string[]> =>
    client.get<string[]>(`${API_BASE}/instance/peers`),

  // Get instance activity
  activity: (): Promise<Array<{ week: number; statuses: number; logins: number; registrations: number }>> =>
    client.get(`${API_BASE}/instance/activity`),

  // Get extended instance information (v2)
  getV2: (): Promise<InstanceInfo & { thumbnail?: string; contact?: { email: string; account: unknown } }> =>
    client.get(`${API_BASE}/instance`),
};

export const searchApi = {
  // Search for content (v2)
  search: (params: {
    q: string;
    type?: 'accounts' | 'statuses' | 'hashtags';
    limit?: number;
    offset?: number;
    following?: boolean;
    account_id?: string;
    max_id?: string;
    min_id?: string;
    exclude_unreviewed?: boolean;
  }): Promise<SearchResults> =>
    client.get<SearchResults>(`${API_BASE}/v2/search`, params),

  // Search for accounts
  accounts: (params: { q: string; limit?: number; offset?: number; following?: boolean }) =>
    client.get(`${API_BASE}/accounts/search`, params),
};

export const preferencesApi = {
  // Get user preferences
  get: (): Promise<Preferences> =>
    client.get<Preferences>(`${API_BASE}/preferences`),

  // Get followed tags
  followedTags: () =>
    client.get(`${API_BASE}/followed_tags`),
};

export const listsApi = {
  // Get all lists
  list: () =>
    client.get(`${API_BASE}/lists`),

  // Get a list
  get: (id: string) =>
    client.get(`${API_BASE}/lists/${id}`),

  // Create a list
  create: (data: { title: string; replies_policy?: 'followed' | 'list' | 'none' }) =>
    client.post(`${API_BASE}/lists`, data),

  // Update a list
  update: (id: string, data: { title: string; replies_policy?: 'followed' | 'list' | 'none' }) =>
    client.put(`${API_BASE}/lists/${id}`, data),

  // Delete a list
  delete: (id: string) =>
    client.delete(`${API_BASE}/lists/${id}`),

  // Get accounts in a list
  accounts: (id: string, params?: { max_id?: string; min_id?: string; limit?: number }) =>
    client.paged(`${API_BASE}/lists/${id}/accounts`, params),

  // Add accounts to a list
  addAccounts: (id: string, accountIds: string[]) =>
    client.post(`${API_BASE}/lists/${id}/accounts`, { account_ids: accountIds }),

  // Remove accounts from a list
  removeAccounts: (id: string, accountIds: string[]) =>
    client.delete(`${API_BASE}/lists/${id}/accounts?account_ids[]=${accountIds.join('&account_ids[]=')}`),
};

export const pollsApi = {
  // Get a poll
  get: (id: string) =>
    client.get(`${API_BASE}/polls/${id}`),

  // Vote in a poll
  vote: (id: string, choices: number[]) =>
    client.post(`${API_BASE}/polls/${id}/votes`, { choices }),
};

export const filtersApi = {
  // Get all filters
  list: () =>
    client.get(`${API_BASE}/filters`),

  // Get a filter
  get: (id: string) =>
    client.get(`${API_BASE}/filters/${id}`),

  // Create a filter
  create: (data: { title: string; context: string[]; expires_in?: number; filter_action?: 'warn' | 'hide' }) =>
    client.post(`${API_BASE}/filters`, data),

  // Update a filter
  update: (id: string, data: { title?: string; context?: string[]; expires_in?: number; filter_action?: 'warn' | 'hide' }) =>
    client.put(`${API_BASE}/filters/${id}`, data),

  // Delete a filter
  delete: (id: string) =>
    client.delete(`${API_BASE}/filters/${id}`),
};

export const bookmarksApi = {
  // Get bookmarked statuses
  list: (params?: { max_id?: string; min_id?: string; limit?: number }) =>
    client.paged(`${API_BASE}/bookmarks`, params),
};

export const favouritesApi = {
  // Get favourited statuses
  list: (params?: { max_id?: string; min_id?: string; limit?: number }) =>
    client.paged(`${API_BASE}/favourites`, params),
};
