package org.joinmastodon.federation.web;

import org.joinmastodon.activitypub.model.Actor;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.federation.service.ActivityPubMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for serving ActivityPub Actor profiles.
 * Handles requests to /users/:username for actor discovery.
 */
@RestController
@RequestMapping("/users")
public class ActorController {

    private final AccountService accountService;
    private final ActivityPubMapper activityPubMapper;

    public ActorController(AccountService accountService, ActivityPubMapper activityPubMapper) {
        this.accountService = accountService;
        this.activityPubMapper = activityPubMapper;
    }

    /**
     * Serve an actor's ActivityPub profile.
     * This endpoint is called by remote instances to discover local users.
     *
     * @param username the local username
     * @return the Actor profile in ActivityPub format
     */
    @GetMapping(value = "/{username}", produces = {
            "application/activity+json",
            "application/ld+json",
            "application/json"
    })
    public ResponseEntity<Actor> getActor(@PathVariable String username) {
        // Only serve local accounts
        Account account = accountService.findLocalAccountByUsername(username)
                .orElse(null);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Actor actor = activityPubMapper.toActor(account);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/activity+json"))
                .body(actor);
    }

    /**
     * Serve an actor's followers collection.
     *
     * @param username the local username
     * @return the followers collection
     */
    @GetMapping(value = "/{username}/followers", produces = {
            "application/activity+json",
            "application/ld+json",
            "application/json"
    })
    public ResponseEntity<?> getFollowers(@PathVariable String username) {
        Account account = accountService.findLocalAccountByUsername(username)
                .orElse(null);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Return a minimal OrderedCollection for now
        // Full implementation would paginate through followers
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/activity+json"))
                .body(java.util.Map.of(
                        "@context", "https://www.w3.org/ns/activitystreams",
                        "type", "OrderedCollection",
                        "totalItems", account.getFollowersCount(),
                        "first", java.util.Map.of(
                                "type", "OrderedCollectionPage",
                                "totalItems", account.getFollowersCount()
                        )
                ));
    }

    /**
     * Serve an actor's following collection.
     *
     * @param username the local username
     * @return the following collection
     */
    @GetMapping(value = "/{username}/following", produces = {
            "application/activity+json",
            "application/ld+json",
            "application/json"
    })
    public ResponseEntity<?> getFollowing(@PathVariable String username) {
        Account account = accountService.findLocalAccountByUsername(username)
                .orElse(null);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Return a minimal OrderedCollection for now
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/activity+json"))
                .body(java.util.Map.of(
                        "@context", "https://www.w3.org/ns/activitystreams",
                        "type", "OrderedCollection",
                        "totalItems", account.getFollowingCount(),
                        "first", java.util.Map.of(
                                "type", "OrderedCollectionPage",
                                "totalItems", account.getFollowingCount()
                        )
                ));
    }
}
