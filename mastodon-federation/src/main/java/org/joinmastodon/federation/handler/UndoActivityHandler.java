package org.joinmastodon.federation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.activitypub.model.ActivityType;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Favourite;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.FavouriteService;
import org.joinmastodon.core.service.FollowService;
import org.joinmastodon.core.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles incoming Undo activities from remote instances.
 * Used to undo previous activities like Follow, Like, Announce, Block.
 */
@Service
public class UndoActivityHandler implements ActivityHandler {
    private static final Logger log = LoggerFactory.getLogger(UndoActivityHandler.class);

    private final FollowService followService;
    private final FavouriteService favouriteService;
    private final StatusService statusService;
    private final AccountService accountService;

    public UndoActivityHandler(FollowService followService,
                               FavouriteService favouriteService,
                               StatusService statusService,
                               AccountService accountService) {
        this.followService = followService;
        this.favouriteService = favouriteService;
        this.statusService = statusService;
        this.accountService = accountService;
    }

    @Override
    public void handle(JsonNode activityJson, Account actor, Account localRecipient) {
        if (actor == null) {
            log.warn("Cannot process Undo activity without actor");
            return;
        }

        // Get the object being undone
        JsonNode objectNode = activityJson.get("object");
        if (objectNode == null) {
            log.warn("Undo activity missing object");
            return;
        }

        // Determine the type of activity being undone
        String objectType = objectNode.has("type") ? objectNode.get("type").asText() : null;
        ActivityType activityType = ActivityType.fromString(objectType);

        if (activityType == null) {
            // Try to infer from the object structure
            if (objectNode.has("object")) {
                // Likely an Undo of a relationship activity (Follow, Like, Announce)
                handleUndoRelationship(objectNode, actor);
            } else {
                log.warn("Cannot determine type of Undo object: {}", objectType);
            }
            return;
        }

        log.info("Processing Undo of {} from {}", activityType, actor.getAcct());

        switch (activityType) {
            case FOLLOW -> handleUndoFollow(objectNode, actor);
            case LIKE -> handleUndoLike(objectNode, actor);
            case ANNOUNCE -> handleUndoAnnounce(objectNode, actor);
            case BLOCK -> handleUndoBlock(objectNode, actor);
            default -> log.warn("Unhandled Undo type: {}", activityType);
        }
    }

    private void handleUndoRelationship(JsonNode objectNode, Account actor) {
        String objectType = objectNode.has("type") ? objectNode.get("type").asText() : null;
        ActivityType activityType = ActivityType.fromString(objectType);

        if (activityType != null) {
            switch (activityType) {
                case FOLLOW -> handleUndoFollow(objectNode, actor);
                case LIKE -> handleUndoLike(objectNode, actor);
                case ANNOUNCE -> handleUndoAnnounce(objectNode, actor);
                default -> log.warn("Unhandled Undo relationship type: {}", activityType);
            }
        }
    }

    private void handleUndoFollow(JsonNode followActivity, Account actor) {
        // Get the target of the original follow
        String targetUrl = followActivity.has("object") ? 
                followActivity.get("object").asText() : null;
        
        if (targetUrl == null) {
            log.warn("Undo Follow missing object (target)");
            return;
        }

        // Find the target account
        Optional<Account> targetOpt = accountService.findByActorUri(targetUrl);
        if (targetOpt.isEmpty()) {
            log.debug("Target account not found for Undo Follow: {}", targetUrl);
            return;
        }

        Account target = targetOpt.get();

        // Remove the follow relationship
        followService.unfollow(actor, target);
        log.info("Unfollowed: {} -> {}", actor.getAcct(), target.getAcct());
    }

    private void handleUndoLike(JsonNode likeActivity, Account actor) {
        // Get the target of the original like
        String objectUrl = likeActivity.has("object") ? 
                likeActivity.get("object").asText() : null;
        
        if (objectUrl == null) {
            log.warn("Undo Like missing object");
            return;
        }

        // Find the status by URI
        Optional<Status> statusOpt = statusService.findByUri(objectUrl);
        if (statusOpt.isEmpty()) {
            log.debug("Status not found for Undo Like: {}", objectUrl);
            return;
        }

        Status status = statusOpt.get();

        // Remove the favourite
        Optional<Favourite> favourite = favouriteService.findByAccountAndStatus(actor, status);
        if (favourite.isPresent()) {
            favouriteService.delete(favourite.get());
            log.info("Unliked: {} -> {}", actor.getAcct(), status.getId());
        }
    }

    private void handleUndoAnnounce(JsonNode announceActivity, Account actor) {
        // Get the target of the original announce
        String objectUrl = announceActivity.has("object") ? 
                announceActivity.get("object").asText() : null;
        
        if (objectUrl == null) {
            log.warn("Undo Announce missing object");
            return;
        }

        // Find the original status
        Optional<Status> originalStatusOpt = statusService.findByUri(objectUrl);
        if (originalStatusOpt.isEmpty()) {
            log.debug("Original status not found for Undo Announce: {}", objectUrl);
            return;
        }

        // Find the boost status by this actor
        Status originalStatus = originalStatusOpt.get();
        Optional<Status> boostOpt = statusService.findByAccountAndReblog(actor, originalStatus);
        
        if (boostOpt.isPresent()) {
            statusService.delete(boostOpt.get());
            log.info("Unannounced: {} -> {}", actor.getAcct(), objectUrl);
        }
    }

    private void handleUndoBlock(JsonNode blockActivity, Account actor) {
        // Block undo handling would go here
        // This would remove the block relationship
        log.debug("Undo Block from {}", actor.getAcct());
    }
}
