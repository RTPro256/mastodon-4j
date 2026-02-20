/**
 * Streaming API using WebSocket
 */

import type { Status, Notification, StreamEvent, StreamEventType } from '@/types';

type StreamCallback = (event: StreamEvent) => void;
type StatusCallback = (status: Status) => void;
type NotificationCallback = (notification: Notification) => void;
type DeleteCallback = (statusId: string) => void;

interface StreamingHandlers {
  onStatus?: StatusCallback;
  onNotification?: NotificationCallback;
  onDelete?: DeleteCallback;
  onStatusUpdate?: StatusCallback;
  onError?: (error: Error) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
}

class StreamingConnection {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private handlers: StreamingHandlers = {};
  private streamType: string;
  private baseUrl: string;
  private accessToken: string;

  constructor(streamType: string, baseUrl: string, accessToken: string) {
    this.streamType = streamType;
    this.baseUrl = baseUrl;
    this.accessToken = accessToken;
  }

  connect(handlers: StreamingHandlers = {}): void {
    this.handlers = handlers;
    this.createConnection();
  }

  private createConnection(): void {
    const wsProtocol = this.baseUrl.startsWith('https') ? 'wss' : 'ws';
    const wsUrl = `${wsProtocol}://${this.baseUrl.replace(/^https?:\/\//, '')}/api/v1/streaming?access_token=${this.accessToken}&stream=${this.streamType}`;

    try {
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        console.log(`WebSocket connected: ${this.streamType}`);
        this.reconnectAttempts = 0;
        this.handlers.onConnect?.();
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this.handleMessage(data);
        } catch (err) {
          console.error('Failed to parse WebSocket message:', err);
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        this.handlers.onError?.(new Error('WebSocket error'));
      };

      this.ws.onclose = () => {
        console.log(`WebSocket disconnected: ${this.streamType}`);
        this.handlers.onDisconnect?.();
        this.attemptReconnect();
      };
    } catch (err) {
      console.error('Failed to create WebSocket:', err);
      this.handlers.onError?.(err as Error);
    }
  }

  private handleMessage(data: { event: StreamEventType; payload: string }): void {
    const { event: eventType, payload } = data;

    switch (eventType) {
      case 'update':
        try {
          const status = JSON.parse(payload) as Status;
          this.handlers.onStatus?.(status);
        } catch (err) {
          console.error('Failed to parse status:', err);
        }
        break;

      case 'notification':
        try {
          const notification = JSON.parse(payload) as Notification;
          this.handlers.onNotification?.(notification);
        } catch (err) {
          console.error('Failed to parse notification:', err);
        }
        break;

      case 'delete':
        this.handlers.onDelete?.(payload);
        break;

      case 'status_update':
        try {
          const status = JSON.parse(payload) as Status;
          this.handlers.onStatusUpdate?.(status);
        } catch (err) {
          console.error('Failed to parse status update:', err);
        }
        break;

      default:
        console.log('Unknown stream event:', eventType);
    }
  }

  private attemptReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnect attempts reached');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);

    console.log(`Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);

    setTimeout(() => {
      this.createConnection();
    }, delay);
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}

// Stream types
export type StreamType = 
  | 'user'
  | 'user:notification'
  | 'public'
  | 'public:local'
  | 'public:remote'
  | 'hashtag'
  | 'hashtag:local'
  | 'list';

// Streaming API manager
export const streamingApi = {
  // Create a user stream connection (home timeline + notifications)
  createUserStream: (baseUrl: string, accessToken: string, handlers: StreamingHandlers): StreamingConnection => {
    const connection = new StreamingConnection('user', baseUrl, accessToken);
    connection.connect(handlers);
    return connection;
  },

  // Create a notification-only stream
  createNotificationStream: (baseUrl: string, accessToken: string, handlers: StreamingHandlers): StreamingConnection => {
    const connection = new StreamingConnection('user:notification', baseUrl, accessToken);
    connection.connect(handlers);
    return connection;
  },

  // Create a public timeline stream
  createPublicStream: (baseUrl: string, accessToken: string, handlers: StreamingHandlers, local = false): StreamingConnection => {
    const streamType = local ? 'public:local' : 'public';
    const connection = new StreamingConnection(streamType, baseUrl, accessToken);
    connection.connect(handlers);
    return connection;
  },

  // Create a hashtag stream
  createHashtagStream: (baseUrl: string, accessToken: string, hashtag: string, handlers: StreamingHandlers, local = false): StreamingConnection => {
    const streamType = local ? `hashtag:local&tag=${hashtag}` : `hashtag&tag=${hashtag}`;
    const connection = new StreamingConnection(streamType, baseUrl, accessToken);
    connection.connect(handlers);
    return connection;
  },

  // Create a list stream
  createListStream: (baseUrl: string, accessToken: string, listId: string, handlers: StreamingHandlers): StreamingConnection => {
    const connection = new StreamingConnection(`list&list=${listId}`, baseUrl, accessToken);
    connection.connect(handlers);
    return connection;
  },
};

export { StreamingConnection };
