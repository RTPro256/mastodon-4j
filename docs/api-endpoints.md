# API Endpoints Reference

This document outlines the REST API endpoints implemented in mastodon_4j, following the Mastodon API v1 and v2 specification.

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
- `GET /api/v1/accounts/relationships` - Get relationships with other accounts
- `GET /api/v1/accounts/:id/identity_proofs` - Get identity proofs (stub)

## Follow Requests
- `GET /api/v1/follow_requests` - Get pending follow requests
- `POST /api/v1/follow_requests/:id/authorize` - Accept a follow request
- `POST /api/v1/follow_requests/:id/reject` - Reject a follow request

## Domain Blocks
- `GET /api/v1/domain_blocks` - Get user's blocked domains
- `POST /api/v1/domain_blocks` - Block a domain
- `DELETE /api/v1/domain_blocks` - Unblock a domain

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
- `POST /api/v1/statuses/:id/pin` - Pin a status to profile
- `POST /api/v1/statuses/:id/unpin` - Unpin a status from profile

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
- `GET /api/v2/search` - Search for content (accounts, statuses, hashtags)
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
- `POST /api/v2/media` - Upload media attachment (async)

## OAuth & Apps
- `POST /api/v1/apps` - Create an application
- `POST /oauth/token` - Obtain OAuth token
- `POST /oauth/revoke` - Revoke OAuth token
- `GET /oauth/authorize` - OAuth authorization page

## Lists
- `GET /api/v1/lists` - Get user's lists
- `GET /api/v1/lists/:id` - Get a list
- `POST /api/v1/lists` - Create a list
- `PUT /api/v1/lists/:id` - Update a list
- `DELETE /api/v1/lists/:id` - Delete a list
- `GET /api/v1/lists/:id/accounts` - Get accounts in list
- `POST /api/v1/lists/:id/accounts` - Add accounts to list
- `DELETE /api/v1/lists/:id/accounts` - Remove accounts from list

## Filters (v2)
- `GET /api/v2/filters` - Get user's filters
- `POST /api/v2/filters` - Create a filter
- `GET /api/v2/filters/:id` - Get a filter
- `PUT /api/v2/filters/:id` - Update a filter
- `DELETE /api/v2/filters/:id` - Delete a filter
- `GET /api/v2/filters/:id/keywords` - Get filter keywords
- `POST /api/v2/filters/:id/keywords` - Add filter keyword

## Admin Endpoints

### Account Management
- `GET /api/v1/admin/accounts` - List accounts (admin)
- `GET /api/v1/admin/accounts/:id` - Get account details (admin)
- `POST /api/v1/admin/accounts/:id/action` - Perform moderation action
- `POST /api/v1/admin/accounts/:id/enable` - Enable disabled account
- `POST /api/v1/admin/accounts/:id/approve` - Approve pending account
- `POST /api/v1/admin/accounts/:id/reject` - Reject pending account

### Report Management
- `GET /api/v1/admin/reports` - List reports (admin)
- `GET /api/v1/admin/reports/:id` - Get report details (admin)
- `POST /api/v1/admin/reports/:id/assign_to_self` - Assign report to self
- `POST /api/v1/admin/reports/:id/unassign` - Unassign report
- `POST /api/v1/admin/reports/:id/resolve` - Resolve report
- `POST /api/v1/admin/reports/:id/reopen` - Reopen report

### Domain Blocks
- `GET /api/v1/admin/domain_blocks` - List instance-level domain blocks
- `GET /api/v1/admin/domain_blocks/:id` - Get domain block details
- `POST /api/v1/admin/domain_blocks` - Create domain block
- `PUT /api/v1/admin/domain_blocks/:id` - Update domain block
- `DELETE /api/v1/admin/domain_blocks/:id` - Remove domain block

### Instance Settings
- `GET /api/v1/admin/instance` - Get instance settings
- `PUT /api/v1/admin/instance` - Update instance settings

### Configuration Management
- `GET /api/v1/admin/config/status` - Get configuration validation status summary
- `GET /api/v1/admin/config/validate` - Get full configuration validation result
- `GET /api/v1/admin/config/categories` - Get list of configuration categories
- `GET /api/v1/admin/config/validate/{category}` - Validate specific category (database, email, federation, security, media, server)

## Instance Information
- `GET /api/v1/instance` - Get instance information
- `GET /api/v1/instance/peers` - Get instance peers
- `GET /api/v1/instance/activity` - Get instance activity
- `GET /api/v1/instance/rules` - Get instance rules

## Polls
- `GET /api/v1/polls/:id` - Get a poll
- `POST /api/v1/polls/:id/votes` - Vote in a poll

## Bookmarks & Favourites
- `GET /api/v1/bookmarks` - Get user's bookmarked statuses
- `GET /api/v1/favourites` - Get user's favourited statuses

## Featured Tags
- `GET /api/v1/featured_tags` - Get user's featured tags
- `POST /api/v1/featured_tags` - Create featured tag
- `DELETE /api/v1/featured_tags/:id` - Delete featured tag

## Preferences
- `GET /api/v1/preferences` - Get user preferences

## Suggestions
- `GET /api/v1/suggestions` - Get account suggestions
- `DELETE /api/v1/suggestions/:id` - Remove suggestion

## Endorsements
- `GET /api/v1/endorsements` - Get endorsed accounts

## Markers
- `GET /api/v1/markers` - Get timeline position markers
- `POST /api/v1/markers` - Save timeline position markers

## Scheduled Statuses
- `GET /api/v1/scheduled_statuses` - Get scheduled statuses
- `GET /api/v1/scheduled_statuses/:id` - Get scheduled status
- `PUT /api/v1/scheduled_statuses/:id` - Update scheduled status
- `DELETE /api/v1/scheduled_statuses/:id` - Delete scheduled status

## Conversation (v1)
- `GET /api/v1/conversations` - Get conversations
- `DELETE /api/v1/conversations/:id` - Delete conversation

## Push Notifications
- `POST /api/v1/push/subscription` - Create push subscription
- `GET /api/v1/push/subscription` - Get push subscription
- `PUT /api/v1/push/subscription` - Update push subscription
- `DELETE /api/v1/push/subscription` - Remove push subscription

## Implementation Notes

1. All authenticated endpoints require OAuth 2.0 bearer token
2. Most endpoints support pagination via `limit`, `max_id`, and `since_id` parameters
3. Rate limiting is implemented per user/IP
4. All responses are in JSON format
5. Follow RESTful conventions for HTTP methods and status codes
6. Status counts (favourites, reblogs, replies) are computed dynamically
7. Pinned status tracking is supported via `/api/v1/statuses/:id/pin`
