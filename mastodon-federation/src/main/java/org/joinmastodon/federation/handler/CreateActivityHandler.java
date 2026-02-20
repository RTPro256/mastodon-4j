package org.joinmastodon.federation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.activitypub.model.Note;
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
 * Handles incoming Create activities from remote instances.
 * Typically contains a Note object representing a new status.
 */
@Service
public class CreateActivityHandler implements ActivityHandler {
    private static final Logger log = LoggerFactory.getLogger(CreateActivityHandler.class);

    private final StatusService statusService;

    public CreateActivityHandler(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public void handle(JsonNode activityJson, Account actor, Account localRecipient) {
        if (actor == null) {
            log.warn("Cannot process Create activity without actor");
            return;
        }

        // Get the object being created
        JsonNode objectNode = activityJson.get("object");
        if (objectNode == null || !objectNode.isObject()) {
            log.warn("Create activity missing object");
            return;
        }

        String objectType = objectNode.has("type") ? objectNode.get("type").asText() : null;
        if (!"Note".equals(objectType)) {
            log.debug("Create activity for non-Note type: {}", objectType);
            // Could be other types like Article, Question, etc.
            return;
        }

        // Parse the Note
        String noteId = objectNode.has("id") ? objectNode.get("id").asText() : null;
        if (noteId == null) {
            log.warn("Create Note activity missing id");
            return;
        }

        // Check if we already have this status
        Optional<Status> existingStatus = statusService.findByUri(noteId);
        if (existingStatus.isPresent()) {
            log.debug("Status already exists: {}", noteId);
            return;
        }

        log.info("Processing Create Note from {}: {}", actor.getAcct(), noteId);

        // Extract Note fields
        String content = objectNode.has("content") ? objectNode.get("content").asText() : "";
        String attributedTo = objectNode.has("attributedTo") ? objectNode.get("attributedTo").asText() : null;
        Instant published = objectNode.has("published") ? 
                Instant.parse(objectNode.get("published").asText()) : Instant.now();
        String inReplyTo = objectNode.has("inReplyTo") ? objectNode.get("inReplyTo").asText() : null;

        // Verify the actor matches the attributedTo
        if (attributedTo != null && !attributedTo.equals(actor.getActorUri())) {
            log.warn("Create Note attributedTo {} does not match actor {}", attributedTo, actor.getActorUri());
            return;
        }

        // Determine visibility from to/cc fields
        Visibility visibility = determineVisibility(objectNode);

        // Create the status
        Status status = new Status();
        status.setAccount(actor);
        status.setContent(content);
        status.setUri(noteId);
        status.setUrl(noteId);
        status.setCreatedAt(published);
        status.setVisibility(visibility);

        // Handle reply
        if (inReplyTo != null) {
            Optional<Status> parentStatus = statusService.findByUri(inReplyTo);
            if (parentStatus.isPresent()) {
                status.setInReplyToId(parentStatus.get().getId());
                status.setInReplyToAccountId(parentStatus.get().getAccount().getId());
            }
        }

        // Save the status
        statusService.save(status);
        log.info("Created remote status: {} from {}", noteId, actor.getAcct());
    }

    private Visibility determineVisibility(JsonNode objectNode) {
        JsonNode toNode = objectNode.get("to");
        JsonNode ccNode = objectNode.get("cc");

        boolean isPublic = false;
        boolean isUnlisted = false;
        boolean isFollowers = false;
        boolean isDirect = false;

        // Check 'to' field
        if (toNode != null && toNode.isArray()) {
            for (JsonNode to : toNode) {
                String toStr = to.asText();
                if ("https://www.w3.org/ns/activitystreams#Public".equals(toStr)) {
                    isPublic = true;
                } else if (toStr.contains("/followers")) {
                    isFollowers = true;
                }
            }
        }

        // Check 'cc' field
        if (ccNode != null && ccNode.isArray()) {
            for (JsonNode cc : ccNode) {
                String ccStr = cc.asText();
                if ("https://www.w3.org/ns/activitystreams#Public".equals(ccStr)) {
                    isUnlisted = true;
                }
            }
        }

        // Determine visibility
        if (isPublic) {
            return Visibility.PUBLIC;
        } else if (isUnlisted) {
            return Visibility.UNLISTED;
        } else if (isFollowers) {
            return Visibility.PRIVATE;
        } else {
            return Visibility.DIRECT;
        }
    }
}
