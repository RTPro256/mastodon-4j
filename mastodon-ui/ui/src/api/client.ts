/**
 * Base API client with authentication handling
 */

import type { ErrorResponse, PagedResponse, PaginationParams } from '@/types';

const API_BASE = '/api/v1';
const OAUTH_BASE = '/oauth';

export class ApiError extends Error {
  constructor(
    public status: number,
    public data: ErrorResponse
  ) {
    super(data.error);
    this.name = 'ApiError';
  }
}

interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined>;
}

function getStoredToken(): string | null {
  return localStorage.getItem('access_token');
}

function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem('access_token', token);
  } else {
    localStorage.removeItem('access_token');
  }
}

function clearStoredToken(): void {
  localStorage.removeItem('access_token');
  localStorage.removeItem('token_created_at');
  localStorage.removeItem('token_scope');
}

function buildQueryString(params: Record<string, string | number | boolean | undefined>): string {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      searchParams.append(key, String(value));
    }
  });
  return searchParams.toString();
}

async function request<T>(
  endpoint: string,
  options: RequestOptions = {}
): Promise<T> {
  const { params, ...fetchOptions } = options;
  
  let url = endpoint;
  if (params) {
    const queryString = buildQueryString(params);
    if (queryString) {
      url += `?${queryString}`;
    }
  }

  const token = getStoredToken();
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    ...fetchOptions,
    headers,
  });

  if (!response.ok) {
    let errorData: ErrorResponse;
    try {
      errorData = await response.json();
    } catch {
      errorData = { error: `HTTP ${response.status}: ${response.statusText}` };
    }
    throw new ApiError(response.status, errorData);
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

// Parse Link header for pagination
function parseLinkHeader(linkHeader: string | null): { next: string | null; prev: string | null } {
  const result = { next: null as string | null, prev: null as string | null };
  
  if (!linkHeader) return result;

  const links = linkHeader.split(',').map(link => link.trim());
  
  for (const link of links) {
    const match = link.match(/<([^>]+)>;\s*rel="([^"]+)"/);
    if (match) {
      const [, url, rel] = match;
      if (rel === 'next') {
        const nextMatch = url.match(/[?&]max_id=([^&]+)/);
        if (nextMatch) result.next = nextMatch[1];
      } else if (rel === 'prev') {
        const prevMatch = url.match(/[?&]since_id=([^&]+)/);
        if (prevMatch) result.prev = prevMatch[1];
      }
    }
  }
  
  return result;
}

async function requestWithPagination<T>(
  endpoint: string,
  params: PaginationParams = {}
): Promise<PagedResponse<T>> {
  const token = getStoredToken();
  const queryString = buildQueryString(params as Record<string, string | number | boolean | undefined>);
  const url = queryString ? `${endpoint}?${queryString}` : endpoint;

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };

  if (token) {
    (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(url, { headers });

  if (!response.ok) {
    let errorData: ErrorResponse;
    try {
      errorData = await response.json();
    } catch {
      errorData = { error: `HTTP ${response.status}: ${response.statusText}` };
    }
    throw new ApiError(response.status, errorData);
  }

  const items = await response.json();
  const linkHeader = response.headers.get('Link');
  const { next, prev } = parseLinkHeader(linkHeader);

  return { items, next, prev };
}

// Export utilities
export const client = {
  get: <T>(endpoint: string, params?: Record<string, string | number | boolean | undefined>) =>
    request<T>(endpoint, { method: 'GET', params }),
  
  post: <T>(endpoint: string, body?: unknown) =>
    request<T>(endpoint, {
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    }),
  
  put: <T>(endpoint: string, body?: unknown) =>
    request<T>(endpoint, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    }),
  
  patch: <T>(endpoint: string, body?: unknown) =>
    request<T>(endpoint, {
      method: 'PATCH',
      body: body ? JSON.stringify(body) : undefined,
    }),
  
  delete: <T>(endpoint: string) =>
    request<T>(endpoint, { method: 'DELETE' }),
  
  paged: <T>(endpoint: string, params?: PaginationParams) =>
    requestWithPagination<T>(endpoint, params),
  
  // For multipart form data (file uploads)
  postFormData: <T>(endpoint: string, formData: FormData) => {
    const token = getStoredToken();
    const headers: HeadersInit = {};
    if (token) {
      (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
    }
    return request<T>(endpoint, {
      method: 'POST',
      headers,
      body: formData,
    });
  },
};

export const auth = {
  getToken: getStoredToken,
  setToken: setStoredToken,
  clearToken: clearStoredToken,
  isAuthenticated: () => !!getStoredToken(),
};

export { API_BASE, OAUTH_BASE };
