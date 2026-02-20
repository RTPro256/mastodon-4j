package org.joinmastodon.federation.web;

import org.joinmastodon.activitypub.model.Create;
import org.joinmastodon.activitypub.model.OrderedCollection;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.StatusService;
import org.joinmastodon.federation.service.ActivityPubMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Controller for serving ActivityPub outboxes.
 * Handles requests to /users/:username/outbox for serving user activities.
 */
@RestController
@RequestMapping("/users")
public class OutboxController {

    private final AccountService accountService;
    private final StatusService statusService;
    private final ActivityPubMapper activityPubMapper;

    public OutboxController(AccountService accountService,
                           StatusService statusService,
                           ActivityPubMapper activityPubMapper) {
        this.accountService = accountService;
        this.statusService = statusService;
        this.activityPubMapper = activityPubMapper;
    }

    /**
     * Serve a user's outbox.
     * Returns an OrderedCollection of the user's public activities.
     *
     * @param username the local username
     * @param page whether to return a specific page
     * @param minId minimum ID for pagination
     * @param maxId maximum ID for pagination
     * @return the outbox collection or page
     */
    @GetMapping(value = "/{username}/outbox", produces = {
            "application/activity+json",
            "application/ld+json",
            "application/json"
    })
    public ResponseEntity<?> getOutbox(@PathVariable String username,
                                       @RequestParam(required = false) Boolean page,
                                       @RequestParam(required = false) Long minId,
                                       @RequestParam(required = false) Long maxId) {
        // Only serve local accounts
        Account account = accountService.findLocalAccountByUsername(username)
                .orElse(null);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // If page is requested, return a page of activities
        if (Boolean.TRUE.equals(page)) {
            return getOutboxPage(account, minId, maxId);
        }

        // Return the collection metadata
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/activity+json"))
                .body(java.util.Map.of(
                        "@context", "https://www.w3.org/ns/activitystreams",
                        "type", "OrderedCollection",
                        "totalItems", account.getStatusesCount(),
                        "first", java.util.Map.of(
                                "type", "OrderedCollectionPage",
                                "id", buildOutboxUrl(username) + "?page=true"
                        ),
                        "last", java.util.Map.of(
                                "type", "OrderedCollectionPage",
                                "id", buildOutboxUrl(username) + "?page=true&min_id=0"
                        )
                ));
    }

    /**
     * Return a page of outbox activities.
     */
    private ResponseEntity<?> getOutboxPage(Account account, Long minId, Long maxId) {
        // Get the user's public statuses
        List<Status> statuses = statusService.findByAccountWithCursor(
                account,
                maxId,
                minId,
                PageRequest.of(0, 20)
        );

        // Filter to only public/unlisted statuses
        List<Status> publicStatuses = statuses.stream()
                .filter(s -> s.getVisibility() == org.joinmastodon.core.model.Visibility.PUBLIC ||
                            s.getVisibility() == org.joinmastodon.core.model.Visibility.UNLISTED)
                .toList();

        // Convert to Create activities
        OrderedCollection<Create> collection = activityPubMapper.toOutbox(account, publicStatuses);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/activity+json"))
                .body(collection);
    }

    /**
     * Handle client-submitted activities to the outbox.
     * This is for authorized clients to post new activities.
     *
     * Note: Full implementation would require authentication and
     * validation of the submitted activity.
     *
     * @param username the local username
     * @param activity the activity to process
     * @return 201 Created with the activity ID
     */
    @PostMapping(value = "/{username}/outbox", consumes = {
            "application/activity+json",
            "application/ld+json",
            "application/json"
    })
    public ResponseEntity<?> postOutbox(@PathVariable String username,
                                        @RequestBody Object activity) {
        // Verify the local user exists
        Optional<Account> localAccount = accountService.findLocalAccountByUsername(username);
        if (localAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Note: Full implementation would:
        // 1. Verify authentication (the user is posting to their own outbox)
        // 2. Validate the activity
        // 3. Process the activity (create status, follow, etc.)
        // 4. Deliver to recipients

        // For now, return 501 Not Implemented
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(java.util.Map.of("error", "Outbox POST not yet implemented"));
    }

    private String buildOutboxUrl(String username) {
        return "/users/" + username + "/outbox";
    }
}
