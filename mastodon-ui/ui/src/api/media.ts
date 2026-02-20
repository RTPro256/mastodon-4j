/**
 * Media API endpoints
 */

import { client, API_BASE } from './client';
import type { MediaAttachment } from '@/types';

const MEDIA_BASE = `${API_BASE}/media`;
const V2_MEDIA_BASE = `${API_BASE}/v2/media`;

export const mediaApi = {
  // Upload media attachment (v2 for async processing)
  upload: async (file: File, options?: {
    description?: string;
    focus?: string;
  }): Promise<MediaAttachment> => {
    const formData = new FormData();
    formData.append('file', file);
    if (options?.description) {
      formData.append('description', options.description);
    }
    if (options?.focus) {
      formData.append('focus', options.focus);
    }
    
    return client.postFormData<MediaAttachment>(V2_MEDIA_BASE, formData);
  },

  // Upload media attachment (v1 for sync processing)
  uploadV1: async (file: File, options?: {
    description?: string;
    focus?: string;
  }): Promise<MediaAttachment> => {
    const formData = new FormData();
    formData.append('file', file);
    if (options?.description) {
      formData.append('description', options.description);
    }
    if (options?.focus) {
      formData.append('focus', options.focus);
    }
    
    return client.postFormData<MediaAttachment>(MEDIA_BASE, formData);
  },

  // Get media attachment
  get: (id: string): Promise<MediaAttachment> =>
    client.get<MediaAttachment>(`${MEDIA_BASE}/${id}`),

  // Update media attachment
  update: (id: string, data: {
    description?: string;
    focus?: string;
    thumbnail?: File;
  }): Promise<MediaAttachment> => {
    const formData = new FormData();
    if (data.description) {
      formData.append('description', data.description);
    }
    if (data.focus) {
      formData.append('focus', data.focus);
    }
    if (data.thumbnail) {
      formData.append('thumbnail', data.thumbnail);
    }
    
    return client.postFormData<MediaAttachment>(`${MEDIA_BASE}/${id}`, formData);
  },
};
