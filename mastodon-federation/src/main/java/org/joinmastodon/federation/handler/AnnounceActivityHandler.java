package org.joinmastodon.federation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.joinmastodon.core.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Handles incoming Announce (boost) activities from remote instances.
 * Represents a boost/retweet of a status by a remote user.
 */
@Service
public class AnnounceActivityHandler implements ActivityHandler {
    private static final Logger log = LoggerFactory.getLogger(AnnounceActivityHandler.class);

    private final StatusService statusService;

    public AnnounceActivityHandler(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public void handle(JsonNode activityJson, Account actor, Account localRecipient) {
        if (actor == null) {
            log.warn("Cannot process Announce activity without actor");
            return;
        }

        // Get the object being announced
        String objectUrl = activityJson.has("object") ? activityJson.get("object").asText() : null;
        if (objectUrl == null) {
            log.warn("Announce activity missing object");
            return;
        }

        String activityId = activityJson.has("id") ? activityJson.get("id").asText() : null;
        Instant published = activityJson.has("published") ? 
                Instant.parse(activityJson.get("published").asText()) : Instant.now();

        log.info("Processing Announce from {}: {}", actor.getAcct(), objectUrl);

        // Find the original status
        Optional<Status> originalStatusOpt = statusService.findByUri(objectUrl);
        if (originalStatusOpt.isEmpty()) {
            log.debug("Original status not found for Announce: {}", objectUrl);
            // Could fetch the status from remote, but for now we'll skip
            return;
        }

        Status originalStatus = originalStatusOpt.get();

        // Check if this boost already exists
        Optional<Status> existingBoost = statusService.findByAccountAndReblog(actor, originalStatus);
        if (existingBoost.isPresent()) {
            log.debug("Boost already exists: {} -> {}", actor.getAcct(), objectUrl);
            return;
        }

        // Create the boost status
        Status boost = new Status();
        boost.setAccount(actor);
        boost.setReblog(originalStatus);
        boost.setUri(activityId);
        boost.setUrl(activityId);
        boost.setCreatedAt(published);
        boost.setVisibility(Visibility.PUBLIC); // Boosts are always public
        boost.setContent(""); // Boosts have no content

        // Save the boost
        statusService.save(boost);
        log.info("Created boost: {} -> {}", actor.getAcct(), objectUrl);
    }
}
