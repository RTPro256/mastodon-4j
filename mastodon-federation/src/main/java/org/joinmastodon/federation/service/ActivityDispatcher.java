package org.joinmastodon.federation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joinmastodon.activitypub.model.ActivityType;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.federation.handler.AcceptActivityHandler;
import org.joinmastodon.federation.handler.AnnounceActivityHandler;
import org.joinmastodon.federation.handler.CreateActivityHandler;
import org.joinmastodon.federation.handler.DeleteActivityHandler;
import org.joinmastodon.federation.handler.FollowActivityHandler;
import org.joinmastodon.federation.handler.LikeActivityHandler;
import org.joinmastodon.federation.handler.RejectActivityHandler;
import org.joinmastodon.federation.handler.UndoActivityHandler;
import org.joinmastodon.federation.handler.UpdateActivityHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Dispatches incoming ActivityPub activities to appropriate handlers.
 * Central routing service for all incoming federation activities.
 */
@Service
public class ActivityDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ActivityDispatcher.class);

    private final ObjectMapper objectMapper;
    private final FollowActivityHandler followActivityHandler;
    private final AcceptActivityHandler acceptActivityHandler;
    private final RejectActivityHandler rejectActivityHandler;
    private final UndoActivityHandler undoActivityHandler;
    private final CreateActivityHandler createActivityHandler;
    private final AnnounceActivityHandler announceActivityHandler;
    private final LikeActivityHandler likeActivityHandler;
    private final DeleteActivityHandler deleteActivityHandler;
    private final UpdateActivityHandler updateActivityHandler;

    public ActivityDispatcher(ObjectMapper objectMapper,
                              FollowActivityHandler followActivityHandler,
                              AcceptActivityHandler acceptActivityHandler,
                              RejectActivityHandler rejectActivityHandler,
                              UndoActivityHandler undoActivityHandler,
                              CreateActivityHandler createActivityHandler,
                              AnnounceActivityHandler announceActivityHandler,
                              LikeActivityHandler likeActivityHandler,
                              DeleteActivityHandler deleteActivityHandler,
                              UpdateActivityHandler updateActivityHandler) {
        this.objectMapper = objectMapper;
        this.followActivityHandler = followActivityHandler;
        this.acceptActivityHandler = acceptActivityHandler;
        this.rejectActivityHandler = rejectActivityHandler;
        this.undoActivityHandler = undoActivityHandler;
        this.createActivityHandler = createActivityHandler;
        this.announceActivityHandler = announceActivityHandler;
        this.likeActivityHandler = likeActivityHandler;
        this.deleteActivityHandler = deleteActivityHandler;
        this.updateActivityHandler = updateActivityHandler;
    }

    /**
     * Dispatch an activity to the appropriate handler based on its type.
     *
     * @param activityJson the raw activity JSON
     * @param activityType the parsed activity type
     * @param actor the remote actor who sent the activity (may be null if fetch failed)
     * @param localRecipient the local recipient (for user-specific inbox, null for shared inbox)
     */
    public void dispatch(JsonNode activityJson, ActivityType activityType, Account actor, Account localRecipient) {
        log.debug("Dispatching activity type: {} from actor: {}", activityType, 
                actor != null ? actor.getAcct() : "unknown");

        try {
            switch (activityType) {
                case FOLLOW -> followActivityHandler.handle(activityJson, actor, localRecipient);
                case ACCEPT -> acceptActivityHandler.handle(activityJson, actor, localRecipient);
                case REJECT -> rejectActivityHandler.handle(activityJson, actor, localRecipient);
                case UNDO -> undoActivityHandler.handle(activityJson, actor, localRecipient);
                case CREATE -> createActivityHandler.handle(activityJson, actor, localRecipient);
                case ANNOUNCE -> announceActivityHandler.handle(activityJson, actor, localRecipient);
                case LIKE -> likeActivityHandler.handle(activityJson, actor, localRecipient);
                case DELETE -> deleteActivityHandler.handle(activityJson, actor, localRecipient);
                case UPDATE -> updateActivityHandler.handle(activityJson, actor, localRecipient);
                case BLOCK -> {
                    log.info("Received Block activity from {}", actor != null ? actor.getAcct() : "unknown");
                    // Block activity handling would go here
                }
                default -> log.warn("Unhandled activity type: {}", activityType);
            }
        } catch (Exception e) {
            log.error("Error handling activity type {}: {}", activityType, e.getMessage(), e);
            // Don't rethrow - we've already accepted the activity
        }
    }
}
