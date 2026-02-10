# API Endpoints Reference

This document outlines the REST API endpoints that need to be implemented, following the Mastodon API v1 and v2 specification.

## Account Operations
- `GET /api/v1/accounts/:id` - Get account information
- `GET /api/v1/accounts/verify_credentials` - Get current user account
- `PATCH /api/v1/accounts/update_credentials` - Update current user account
- `GET /api/v1/accounts/:id/statuses` - Get account's statuses
- `GET /api/v1/accounts/:id/followers` - Get account's followers
- `GET /api/v1/accounts/:id/following` - Get accounts that account is following
- `POST /api/v1/accounts/:id/follow` - Follow an account
- `POST /api/v1/accounts/:id/unfollow` - Unfollow an account
- `POST /api/v1/accounts/:id/block` - Block an account
- `POST /api/v1/accounts/:id/unblock` - Unblock an account
- `POST /api/v1/accounts/:id/mute` - Mute an account
- `POST /api/v1/accounts/:id/unmute` - Unmute an account

## Status Operations
- `POST /api/v1/statuses` - Create a new status
- `GET /api/v1/statuses/:id` - Get a status
- `DELETE /api/v1/statuses/:id` - Delete a status
- `GET /api/v1/statuses/:id/context` - Get status context (ancestors/descendants)
- `POST /api/v1/statuses/:id/favourite` - Favourite a status
- `POST /api/v1/statuses/:id/unfavourite` - Unfavourite a status
- `POST /api/v1/statuses/:id/reblog` - Reblog (boost) a status
- `POST /api/v1/statuses/:id/unreblog` - Unreblog a status
- `POST /api/v1/statuses/:id/bookmark` - Bookmark a status
- `POST /api/v1/statuses/:id/unbookmark` - Unbookmark a status

## Timeline Access
- `GET /api/v1/timelines/home` - Get home timeline
- `GET /api/v1/timelines/public` - Get public timeline
- `GET /api/v1/timelines/tag/:hashtag` - Get hashtag timeline
- `GET /api/v1/timelines/list/:list_id` - Get list timeline

## Notifications
- `GET /api/v1/notifications` - Get notifications
- `GET /api/v1/notifications/:id` - Get a single notification
- `POST /api/v1/notifications/clear` - Clear all notifications
- `POST /api/v1/notifications/:id/dismiss` - Dismiss a notification

## Search
- `GET /api/v2/search` - Search for content
- `GET /api/v1/accounts/search` - Search for accounts

## Streaming API
- `GET /api/v1/streaming/user` - Stream user events
- `GET /api/v1/streaming/public` - Stream public events
- `GET /api/v1/streaming/public/local` - Stream local public events
- `GET /api/v1/streaming/hashtag` - Stream hashtag events
- `GET /api/v1/streaming/list` - Stream list events

## Media
- `POST /api/v1/media` - Upload media attachment
- `GET /api/v1/media/:id` - Get media attachment
- `PUT /api/v1/media/:id` - Update media attachment

## OAuth & Apps
- `POST /api/v1/apps` - Create an application
- `POST /oauth/token` - Obtain OAuth token
- `POST /oauth/revoke` - Revoke OAuth token

## Lists
- `GET /api/v1/lists` - Get user's lists
- `GET /api/v1/lists/:id` - Get a list
- `POST /api/v1/lists` - Create a list
- `PUT /api/v1/lists/:id` - Update a list
- `DELETE /api/v1/lists/:id` - Delete a list
- `GET /api/v1/lists/:id/accounts` - Get accounts in list
- `POST /api/v1/lists/:id/accounts` - Add accounts to list
- `DELETE /api/v1/lists/:id/accounts` - Remove accounts from list

## Filters
- `GET /api/v1/filters` - Get user's filters
- `POST /api/v1/filters` - Create a filter
- `GET /api/v1/filters/:id` - Get a filter
- `PUT /api/v1/filters/:id` - Update a filter
- `DELETE /api/v1/filters/:id` - Delete a filter

## Admin Endpoints
- `GET /api/v1/admin/accounts` - Get accounts (admin)
- `GET /api/v1/admin/reports` - Get reports (admin)
- Various other moderation and instance management endpoints

## Instance Information
- `GET /api/v1/instance` - Get instance information
- `GET /api/v1/instance/peers` - Get instance peers
- `GET /api/v1/instance/activity` - Get instance activity

## Polls
- `GET /api/v1/polls/:id` - Get a poll
- `POST /api/v1/polls/:id/votes` - Vote in a poll

## Implementation Notes

1. All authenticated endpoints require OAuth 2.0 bearer token
2. Most endpoints support pagination via `limit`, `max_id`, and `since_id` parameters
3. Rate limiting should be implemented per user/IP
4. All responses should be in JSON format
5. Follow RESTful conventions for HTTP methods and status codes
