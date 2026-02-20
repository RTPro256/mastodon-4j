/**
 * Timeline state management using React Context
 */

import React, { createContext, useContext, useState, useCallback, useMemo, useRef, useEffect } from 'react';
import type { Status, Notification, PagedResponse, PaginationParams } from '@/types';
import { timelinesApi, notificationsApi, streamingApi, StreamingConnection } from '@/api';
import { useAuth } from './AuthContext';

type TimelineType = 'home' | 'public' | 'public:local' | 'hashtag' | 'list' | 'notifications';

interface TimelineState {
  statuses: Status[];
  notifications: Notification[];
  isLoading: boolean;
  isLoadingMore: boolean;
  hasMore: boolean;
  error: string | null;
}

interface TimelineContextValue extends TimelineState {
  loadTimeline: (type: TimelineType, params?: { hashtag?: string; listId?: string }) => Promise<void>;
  loadMore: () => Promise<void>;
  refresh: () => Promise<void>;
  addStatus: (status: Status) => void;
  updateStatus: (status: Status) => void;
  removeStatus: (id: string) => void;
  addNotification: (notification: Notification) => void;
  clearError: () => void;
  currentTimeline: TimelineType;
  currentParams: { hashtag?: string; listId?: string };
}

const TimelineContext = createContext<TimelineContextValue | null>(null);

export function TimelineProvider({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, instance, accessToken } = useAuth();
  
  const [state, setState] = useState<TimelineState>({
    statuses: [],
    notifications: [],
    isLoading: false,
    isLoadingMore: false,
    hasMore: true,
    error: null,
  });

  const [currentTimeline, setCurrentTimeline] = useState<TimelineType>('home');
  const [currentParams, setCurrentParams] = useState<{ hashtag?: string; listId?: string }>({});
  
  const streamingConnection = useRef<StreamingConnection | null>(null);
  const nextId = useRef<string | null>(null);

  // Cleanup streaming on unmount
  useEffect(() => {
    return () => {
      streamingConnection.current?.disconnect();
    };
  }, []);

  // Connect to streaming when authenticated
  useEffect(() => {
    if (isAuthenticated && instance && accessToken) {
      connectStreaming();
    } else {
      streamingConnection.current?.disconnect();
      streamingConnection.current = null;
    }
  }, [isAuthenticated, instance, accessToken]);

  const connectStreaming = useCallback(() => {
    if (!instance || !accessToken) return;

    const baseUrl = instance.urls?.streaming_api || window.location.host;

    streamingConnection.current = streamingApi.createUserStream(
      baseUrl,
      accessToken,
      {
        onStatus: (status) => {
          // Only add to home timeline
          if (currentTimeline === 'home') {
            setState(prev => ({
              ...prev,
              statuses: [status, ...prev.statuses],
            }));
          }
        },
        onNotification: (notification) => {
          setState(prev => ({
            ...prev,
            notifications: [notification, ...prev.notifications],
          }));
        },
        onDelete: (statusId) => {
          setState(prev => ({
            ...prev,
            statuses: prev.statuses.filter(s => s.id !== statusId),
          }));
        },
        onStatusUpdate: (status) => {
          setState(prev => ({
            ...prev,
            statuses: prev.statuses.map(s => s.id === status.id ? status : s),
          }));
        },
        onError: (error) => {
          console.error('Streaming error:', error);
        },
      }
    );
  }, [instance, accessToken, currentTimeline]);

  const loadTimeline = useCallback(async (
    type: TimelineType,
    params?: { hashtag?: string; listId?: string }
  ) => {
    setState(prev => ({ ...prev, isLoading: true, error: null }));
    setCurrentTimeline(type);
    setCurrentParams(params || {});

    try {
      let response: PagedResponse<Status>;

      switch (type) {
        case 'home':
          response = await timelinesApi.home({ limit: 40 });
          break;
        case 'public':
          response = await timelinesApi.public({ limit: 40 });
          break;
        case 'public:local':
          response = await timelinesApi.public({ limit: 40, local: true });
          break;
        case 'hashtag':
          if (!params?.hashtag) throw new Error('Hashtag required');
          response = await timelinesApi.hashtag(params.hashtag, { limit: 40 });
          break;
        case 'list':
          if (!params?.listId) throw new Error('List ID required');
          response = await timelinesApi.list(params.listId, { limit: 40 });
          break;
        case 'notifications':
          const notifResponse = await notificationsApi.list({ limit: 40 });
          setState(prev => ({
            ...prev,
            notifications: notifResponse.items,
            isLoading: false,
            hasMore: !!notifResponse.next,
          }));
          nextId.current = notifResponse.next;
          return;
        default:
          throw new Error(`Unknown timeline type: ${type}`);
      }

      nextId.current = response.next;
      setState(prev => ({
        ...prev,
        statuses: response.items,
        isLoading: false,
        hasMore: !!response.next,
      }));
    } catch (error) {
      console.error('Failed to load timeline:', error);
      setState(prev => ({
        ...prev,
        isLoading: false,
        error: error instanceof Error ? error.message : 'Failed to load timeline',
      }));
    }
  }, []);

  const loadMore = useCallback(async () => {
    if (!nextId.current || state.isLoadingMore || !state.hasMore) return;

    setState(prev => ({ ...prev, isLoadingMore: true }));

    try {
      const params: PaginationParams = { max_id: nextId.current, limit: 40 };
      let response: PagedResponse<Status>;

      switch (currentTimeline) {
        case 'home':
          response = await timelinesApi.home(params);
          break;
        case 'public':
          response = await timelinesApi.public(params);
          break;
        case 'public:local':
          response = await timelinesApi.public({ ...params, local: true });
          break;
        case 'hashtag':
          if (!currentParams.hashtag) throw new Error('Hashtag required');
          response = await timelinesApi.hashtag(currentParams.hashtag, params);
          break;
        case 'list':
          if (!currentParams.listId) throw new Error('List ID required');
          response = await timelinesApi.list(currentParams.listId, params);
          break;
        case 'notifications':
          const notifResponse = await notificationsApi.list(params);
          setState(prev => ({
            ...prev,
            notifications: [...prev.notifications, ...notifResponse.items],
            isLoadingMore: false,
            hasMore: !!notifResponse.next,
          }));
          nextId.current = notifResponse.next;
          return;
        default:
          throw new Error(`Unknown timeline type: ${currentTimeline}`);
      }

      nextId.current = response.next;
      setState(prev => ({
        ...prev,
        statuses: [...prev.statuses, ...response.items],
        isLoadingMore: false,
        hasMore: !!response.next,
      }));
    } catch (error) {
      console.error('Failed to load more:', error);
      setState(prev => ({
        ...prev,
        isLoadingMore: false,
        error: error instanceof Error ? error.message : 'Failed to load more',
      }));
    }
  }, [currentTimeline, currentParams, state.isLoadingMore, state.hasMore]);

  const refresh = useCallback(async () => {
    await loadTimeline(currentTimeline, currentParams);
  }, [loadTimeline, currentTimeline, currentParams]);

  const addStatus = useCallback((status: Status) => {
    setState(prev => ({
      ...prev,
      statuses: [status, ...prev.statuses],
    }));
  }, []);

  const updateStatus = useCallback((status: Status) => {
    setState(prev => ({
      ...prev,
      statuses: prev.statuses.map(s => s.id === status.id ? status : s),
    }));
  }, []);

  const removeStatus = useCallback((id: string) => {
    setState(prev => ({
      ...prev,
      statuses: prev.statuses.filter(s => s.id !== id),
    }));
  }, []);

  const addNotification = useCallback((notification: Notification) => {
    setState(prev => ({
      ...prev,
      notifications: [notification, ...prev.notifications],
    }));
  }, []);

  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }));
  }, []);

  const value = useMemo<TimelineContextValue>(() => ({
    ...state,
    loadTimeline,
    loadMore,
    refresh,
    addStatus,
    updateStatus,
    removeStatus,
    addNotification,
    clearError,
    currentTimeline,
    currentParams,
  }), [
    state,
    loadTimeline,
    loadMore,
    refresh,
    addStatus,
    updateStatus,
    removeStatus,
    addNotification,
    clearError,
    currentTimeline,
    currentParams,
  ]);

  return (
    <TimelineContext.Provider value={value}>
      {children}
    </TimelineContext.Provider>
  );
}

export function useTimeline(): TimelineContextValue {
  const context = useContext(TimelineContext);
  if (!context) {
    throw new Error('useTimeline must be used within a TimelineProvider');
  }
  return context;
}

export { TimelineContext };
