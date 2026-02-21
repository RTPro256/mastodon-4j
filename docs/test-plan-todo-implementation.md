# Test Plan for TODO Implementation

This document outlines the test plan for completing the remaining TODO items identified in VSCode.

## Overview

The following TODO items require implementation and testing:

| ID | File | Line | Description | Priority |
|----|------|------|-------------|----------|
| 1 | AdminAccountController.java | 203 | Track last known IP | Medium |
| 2 | AdminAccountController.java | 206 | Implement email confirmation | High |
| 3 | AdminAccountController.java | 210 | Implement approval workflow | High |
| 4 | AccountController.java | 296 | Implement follow requests | High |
| 5 | AccountController.java | 297 | Implement domain blocks | Medium |
| 6 | ApiMapper.java | 100 | Implement favourites counting | High |
| 7 | ApiMapper.java | 101 | Implement reblogs counting | High |
| 8 | ApiMapper.java | 102 | Implement replies counting | High |
| 9 | ApiMapper.java | 106 | Implement pinned check | Medium |

---

## TODO 1: Track Last Known IP

### Implementation Requirements
- Add `lastSignInIp` field to User entity
- Update authentication flow to capture and store IP address
- Add IP address to AdminAccountDto

### Test Cases
1. **TC-IP-001**: Verify IP is captured on successful login
   - Login with valid credentials
   - Verify `lastSignInIp` is populated in database
   - Verify IP appears in admin API response

2. **TC-IP-002**: Verify IP is updated on subsequent logins
   - Login from different IP (or simulate)
   - Verify `lastSignInIp` is updated

3. **TC-IP-003**: Verify null IP handling for new accounts
   - Create new account
   - Verify admin API returns null for IP before first login

### Files to Modify
- `mastodon-core/src/main/java/org/joinmastodon/core/entity/User.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/auth/BearerTokenAuthenticationFilter.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/api/admin/AdminAccountController.java`
- Database migration for new column

---

## TODO 2: Implement Email Confirmation

### Implementation Requirements
- Add `emailConfirmed` boolean field to User entity
- Add `confirmationToken` field for verification
- Create email confirmation endpoint
- Integrate with registration flow

### Test Cases
1. **TC-EMAIL-001**: Verify new accounts start unconfirmed
   - Register new account
   - Verify `confirmed` is false in admin API

2. **TC-EMAIL-002**: Verify confirmation token generation
   - Register new account
   - Verify confirmation token is generated and stored

3. **TC-EMAIL-003**: Verify email confirmation endpoint
   - Call confirmation endpoint with valid token
   - Verify `confirmed` changes to true

4. **TC-EMAIL-004**: Verify invalid token handling
   - Call confirmation endpoint with invalid token
   - Verify appropriate error response

### Files to Modify
- `mastodon-core/src/main/java/org/joinmastodon/core/entity/User.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/api/OAuthController.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/api/admin/AdminAccountController.java`
- New controller for email confirmation

---

## TODO 3: Implement Approval Workflow

### Implementation Requirements
- Add `approved` boolean field to User entity
- Add `approvedAt` timestamp field
- Create admin endpoint for approving accounts
- Restrict access for unapproved accounts

### Test Cases
1. **TC-APPROVAL-001**: Verify new accounts require approval (if enabled)
   - Register new account with approval required setting
   - Verify `approved` is false initially

2. **TC-APPROVAL-002**: Verify admin approval endpoint
   - Call admin approval endpoint
   - Verify `approved` changes to true
   - Verify `approvedAt` is set

3. **TC-APPROVAL-003**: Verify unapproved account access restriction
   - Attempt to use API with unapproved account
   - Verify appropriate error response

### Files to Modify
- `mastodon-core/src/main/java/org/joinmastodon/core/entity/User.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/api/admin/AdminAccountController.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/auth/BearerTokenAuthenticationFilter.java`

---

## TODO 4: Implement Follow Requests

### Implementation Requirements
- Add follow request entity/table
- Create follow request endpoints (list, accept, reject)
- Update relationship DTO to show request status
- Handle locked account follow flow

### Test Cases
1. **TC-FOLLOW-001**: Verify follow request creation for locked accounts
   - Follow a locked account
   - Verify `requested` is true in relationship response

2. **TC-FOLLOW-002**: Verify follow request list endpoint
   - Create follow request
   - List pending follow requests
   - Verify request appears in list

3. **TC-FOLLOW-003**: Verify follow request acceptance
   - Accept follow request
   - Verify relationship changes to following
   - Verify `requested` is false

4. **TC-FOLLOW-004**: Verify follow request rejection
   - Reject follow request
   - Verify no follow relationship created

### Files to Modify
- `mastodon-core/src/main/java/org/joinmastodon/core/entity/Follow.java` (add pending status)
- `mastodon-web/src/main/java/org/joinmastodon/web/api/AccountController.java`
- `mastodon-core/src/main/java/org/joinmastodon/core/service/FollowService.java`

---

## TODO 5: Implement Domain Blocks

### Implementation Requirements
- Domain block entity already exists (`DomainBlock.java`)
- Create domain block endpoints (block, unblock, list)
- Update relationship DTO
- Filter content from blocked domains

### Test Cases
1. **TC-DOMAIN-001**: Verify domain block creation
   - Block a domain
   - Verify domain appears in block list

2. **TC-DOMAIN-002**: Verify domain block filtering
   - Block a domain
   - Verify statuses from that domain are hidden

3. **TC-DOMAIN-003**: Verify domain unblock
   - Unblock a domain
   - Verify domain removed from block list
   - Verify content is visible again

4. **TC-DOMAIN-004**: Verify relationship DTO shows domain blocking
   - Block domain of a remote account
   - Verify `domainBlocking` is true in relationship

### Files to Modify
- `mastodon-web/src/main/java/org/joinmastodon/web/api/AccountController.java`
- `mastodon-core/src/main/java/org/joinmastodon/core/service/DomainBlockService.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/api/ApiMapper.java`

---

## TODO 6-8: Implement Status Counting

### Implementation Requirements
- Add methods to count favourites, reblogs, and replies for a status
- Update ApiMapper to use actual counts
- Consider caching for performance

### Test Cases
1. **TC-COUNT-001**: Verify favourites count
   - Create status
   - Add favourites
   - Verify count matches in API response

2. **TC-COUNT-002**: Verify reblogs count
   - Create status
   - Add reblogs
   - Verify count matches in API response

3. **TC-COUNT-003**: Verify replies count
   - Create status
   - Add replies
   - Verify count matches in API response

4. **TC-COUNT-004**: Verify counts update correctly
   - Remove a favourite
   - Verify count decrements

### Files to Modify
- `mastodon-core/src/main/java/org/joinmastodon/core/service/StatusService.java`
- `mastodon-core/src/main/java/org/joinmastodon/core/repository/StatusRepository.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/api/ApiMapper.java`

---

## TODO 9: Implement Pinned Check

### Implementation Requirements
- Add pinned status relationship/table
- Create pin/unpin endpoints
- Update ApiMapper to check pinned status

### Test Cases
1. **TC-PIN-001**: Verify pin status
   - Pin a status
   - Verify `pinned` is true in API response

2. **TC-PIN-002**: Verify unpin status
   - Unpin a status
   - Verify `pinned` is false

3. **TC-PIN-003**: Verify pinned statuses list
   - Pin multiple statuses
   - List pinned statuses
   - Verify all appear

### Files to Modify
- `mastodon-core/src/main/java/org/joinmastodon/core/entity/Status.java` (add pinned field or relationship)
- `mastodon-web/src/main/java/org/joinmastodon/web/api/StatusController.java`
- `mastodon-web/src/main/java/org/joinmastodon/web/api/ApiMapper.java`

---

## Execution Order

Recommended implementation order based on dependencies and priority:

1. **Phase 1 - Core Features** (High Priority)
   - TODO 6-8: Status counting (affects user experience)
   - TODO 4: Follow requests (core Mastodon feature)

2. **Phase 2 - Account Management** (High Priority)
   - TODO 2: Email confirmation
   - TODO 3: Approval workflow

3. **Phase 3 - Additional Features** (Medium Priority)
   - TODO 1: Track last known IP
   - TODO 5: Domain blocks
   - TODO 9: Pinned check

---

## Test Execution

For each TODO:
1. Write unit tests first (TDD approach)
2. Implement the feature
3. Run unit tests
4. Write integration tests
5. Run full test suite
6. Update API documentation

## Notes

- All database changes require new Flyway migrations
- Consider backward compatibility for API changes
- Add appropriate logging for new features
- Update CLAUDE.md documentation after implementation
