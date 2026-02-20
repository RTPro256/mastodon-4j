/**
 * TypeScript types for Mastodon API entities
 * Based on docs/ui-contracts.md
 */

// ============================================
// Instance
// ============================================

export interface InstanceInfo {
  uri: string;
  title: string;
  description: string;
  short_description?: string;
  email?: string;
  version: string;
  urls?: {
    streaming_api?: string;
  };
  stats: {
    user_count: number;
    status_count: number;
    domain_count: number;
  };
  configuration?: {
    statuses?: {
      max_characters?: number;
      max_media_attachments?: number;
      characters_reserved_per_url?: number;
    };
    media_attachments?: {
      supported_mime_types?: string[];
      image_size_limit?: number;
      video_size_limit?: number;
    };
    polls?: {
      max_options?: number;
      max_characters_per_option?: number;
      min_expiration?: number;
      max_expiration?: number;
    };
  };
}

// ============================================
// Account
// ============================================

export interface Field {
  name: string;
  value: string;
  verified_at: string | null;
}

export interface Account {
  id: string;
  username: string;
  acct: string;
  display_name: string;
  locked: boolean;
  bot: boolean;
  created_at: string;
  note: string;
  url: string;
  avatar: string;
  header: string;
  followers_count: number;
  following_count: number;
  statuses_count: number;
  fields: Field[];
  emojis?: CustomEmoji[];
  moved?: Account;
  suspended?: boolean;
  silenced?: boolean;
}

export interface CustomEmoji {
  shortcode: string;
  url: string;
  static_url: string;
  visible_in_picker: boolean;
}

// ============================================
// Relationship
// ============================================

export interface Relationship {
  id: string;
  following: boolean;
  followed_by: boolean;
  blocking: boolean;
  muting: boolean;
  requested: boolean;
  domain_blocking: boolean;
  showing_reblogs?: boolean;
  endorsed?: boolean;
  note?: string;
}

// ============================================
// Status
// ============================================

export type Visibility = 'public' | 'unlisted' | 'private' | 'direct';

export interface Mention {
  id: string;
  username: string;
  url: string;
  acct: string;
}

export interface Tag {
  name: string;
  url: string;
}

export interface Status {
  id: string;
  created_at: string;
  in_reply_to_id: string | null;
  in_reply_to_account_id: string | null;
  sensitive: boolean;
  spoiler_text: string;
  visibility: Visibility;
  language: string | null;
  uri: string;
  url: string;
  content: string;
  account: Account;
  media_attachments: MediaAttachment[];
  mentions: Mention[];
  tags: Tag[];
  poll: Poll | null;
  reblog: Status | null;
  reblogged?: boolean;
  favourited?: boolean;
  bookmarked?: boolean;
  muted?: boolean;
  pinned?: boolean;
  favourites_count: number;
  reblogs_count: number;
  replies_count: number;
  application?: Application;
  emojis?: CustomEmoji[];
  filtered?: FilterResult[];
}

export interface Application {
  name: string;
  website?: string;
}

// ============================================
// Media
// ============================================

export type MediaType = 'image' | 'video' | 'gifv' | 'audio' | 'unknown';

export interface MediaMeta {
  original?: MediaDimensions;
  small?: MediaDimensions;
  focus?: { x: number; y: number };
  length?: string;
  duration?: number;
  fps?: number;
  size?: string;
  width?: number;
  height?: number;
  aspect?: number;
  audio_encode?: string;
  audio_bitrate?: string;
  audio_channels?: string;
}

export interface MediaDimensions {
  width: number;
  height: number;
  size?: string;
  aspect?: number;
}

export interface MediaAttachment {
  id: string;
  type: MediaType;
  url: string | null;
  preview_url: string | null;
  remote_url: string | null;
  meta: MediaMeta;
  description: string;
  blurhash: string;
  text_url?: string;
}

// ============================================
// Poll
// ============================================

export interface PollOption {
  title: string;
  votes_count: number;
}

export interface Poll {
  id: string;
  expires_at: string | null;
  expired: boolean;
  multiple: boolean;
  votes_count: number;
  voters_count: number | null;
  voted: boolean | null;
  own_votes: number[] | null;
  options: PollOption[];
  emojis?: CustomEmoji[];
}

// ============================================
// Notification
// ============================================

export type NotificationType = 
  | 'mention'
  | 'status'
  | 'reblog'
  | 'follow'
  | 'follow_request'
  | 'favourite'
  | 'poll'
  | 'update'
  | 'admin.sign_up'
  | 'admin.report';

export interface Notification {
  id: string;
  type: NotificationType;
  created_at: string;
  account: Account;
  status: Status | null;
  report?: Report;
}

// ============================================
// Report
// ============================================

export interface Report {
  id: string;
  action_taken: boolean;
  action_taken_at: string | null;
  category: 'spam' | 'violation' | 'other';
  comment: string;
  forwarded: boolean;
  created_at: string;
  status_ids: string[] | null;
  rule_ids: number[] | null;
  target_account: Account;
}

// ============================================
// Search
// ============================================

export interface SearchResults {
  accounts: Account[];
  statuses: Status[];
  hashtags: Tag[];
}

// ============================================
// OAuth
// ============================================

export interface OAuthApp {
  id: string;
  name: string;
  website?: string;
  client_id: string;
  client_secret: string;
  redirect_uri: string;
}

export interface TokenResponse {
  access_token: string;
  token_type: string;
  scope: string;
  created_at: number;
  refresh_token?: string;
  expires_in?: number;
}

// ============================================
// Error
// ============================================

export interface ErrorResponse {
  error: string;
  error_description?: string;
  details?: Record<string, string>;
}

// ============================================
// Request Types
// ============================================

export interface StatusCreateRequest {
  status: string;
  visibility?: Visibility;
  sensitive?: boolean;
  spoiler_text?: string;
  media_ids?: string[];
  poll?: {
    options: string[];
    expires_in: number;
    multiple?: boolean;
    hide_totals?: boolean;
  };
  in_reply_to_id?: string;
  language?: string;
}

export interface AccountUpdateRequest {
  display_name?: string;
  note?: string;
  avatar?: string;
  header?: string;
  locked?: boolean;
  bot?: boolean;
  discoverable?: boolean;
  hide_collections?: boolean;
  indexable?: boolean;
  fields_attributes?: Array<{
    name: string;
    value: string;
  }>;
}

// ============================================
// Pagination
// ============================================

export interface PaginationParams {
  max_id?: string;
  min_id?: string;
  since_id?: string;
  limit?: number;
}

export interface PagedResponse<T> {
  items: T[];
  next: string | null;
  prev: string | null;
}

// ============================================
// Streaming
// ============================================

export type StreamEventType = 'update' | 'notification' | 'delete' | 'filters_changed' | 'conversation' | 'announcement' | 'encrypted_message' | 'status_update';

export interface StreamEvent {
  event: StreamEventType;
  payload: Status | Notification | string;
}

// ============================================
// Admin
// ============================================

export type AdminAccountStatus = 'active' | 'suspended' | 'silenced' | 'pending' | 'disabled';

export interface AdminAccount {
  id: string;
  username: string;
  domain: string | null;
  status: AdminAccountStatus;
  created_at: string;
  email?: string;
  ip?: string;
  role?: {
    id: string;
    name: string;
    permissions: number;
  };
  invited_by?: Account;
  account: Account;
}

export interface AdminReport {
  id: string;
  action_taken: boolean;
  action_taken_at: string | null;
  category: string;
  comment: string;
  forwarded: boolean;
  created_at: string;
  updated_at: string;
  account: Account;
  target_account: Account;
  assigned_account: Account | null;
  action_taken_by_account: Account | null;
  statuses: Status[];
  rules: Array<{ id: string; text: string }>;
}

// ============================================
// Lists
// ============================================

export interface List {
  id: string;
  title: string;
  replies_policy?: 'followed' | 'list' | 'none';
  exclusive?: boolean;
}

// ============================================
// Filters
// ============================================

export type FilterContext = 'home' | 'notifications' | 'public' | 'thread' | 'account' | 'profile';

export interface Filter {
  id: string;
  title: string;
  context: FilterContext[];
  expires_at: string | null;
  filter_action: 'warn' | 'hide';
  keywords: FilterKeyword[];
  statuses: FilterStatus[];
}

export interface FilterKeyword {
  id: string;
  keyword: string;
  whole_word: boolean;
}

export interface FilterStatus {
  id: string;
  status_id: string;
}

export interface FilterResult {
  filter: Filter;
  keyword_matches?: string[];
  status_matches?: string[];
}

// ============================================
// Preferences
// ============================================

export interface Preferences {
  'posting:default:visibility': Visibility;
  'posting:default:sensitive': boolean;
  'posting:default:language': string | null;
  'reading:expand:media': 'default' | 'show_all' | 'hide_all';
  'reading:expand:spoilers': boolean;
}

// ============================================
// Context (for status threads)
// ============================================

export interface Context {
  ancestors: Status[];
  descendants: Status[];
}
