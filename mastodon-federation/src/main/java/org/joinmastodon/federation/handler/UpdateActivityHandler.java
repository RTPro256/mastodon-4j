package org.joinmastodon.federation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles incoming Update activities from remote instances.
 * Used to update remote profiles, statuses, or other objects.
 */
@Service
public class UpdateActivityHandler implements ActivityHandler {
    private static final Logger log = LoggerFactory.getLogger(UpdateActivityHandler.class);

    private final AccountService accountService;
    private final StatusService statusService;

    public UpdateActivityHandler(AccountService accountService, StatusService statusService) {
        this.accountService = accountService;
        this.statusService = statusService;
    }

    @Override
    public void handle(JsonNode activityJson, Account actor, Account localRecipient) {
        if (actor == null) {
            log.warn("Cannot process Update activity without actor");
            return;
        }

        // Get the object being updated
        JsonNode objectNode = activityJson.get("object");
        if (objectNode == null || !objectNode.isObject()) {
            log.warn("Update activity missing object");
            return;
        }

        String objectId = objectNode.has("id") ? objectNode.get("id").asText() : null;
        String objectType = objectNode.has("type") ? objectNode.get("type").asText() : null;

        if (objectId == null) {
            log.warn("Update activity object missing id");
            return;
        }

        log.info("Processing Update from {}: {} ({})", actor.getAcct(), objectId, objectType);

        // Handle based on object type
        if ("Person".equals(objectType) || "Service".equals(objectType) || 
            "Group".equals(objectType) || "Organization".equals(objectType)) {
            handleActorUpdate(objectNode, actor);
        } else if ("Note".equals(objectType) || "Article".equals(objectType)) {
            handleNoteUpdate(objectNode, actor);
        } else {
            // Try to determine by URL pattern
            if (objectId.contains("/users/") && !objectId.contains("/statuses/")) {
                handleActorUpdate(objectNode, actor);
            } else if (objectId.contains("/statuses/") || objectId.contains("/notes/")) {
                handleNoteUpdate(objectNode, actor);
            } else {
                log.debug("Unhandled Update object type: {} for {}", objectType, objectId);
            }
        }
    }

    private void handleActorUpdate(JsonNode actorNode, Account actor) {
        String actorId = actorNode.has("id") ? actorNode.get("id").asText() : null;
        if (actorId == null) {
            return;
        }

        // Verify the actor matches
        if (!actorId.equals(actor.getActorUri())) {
            log.warn("Update actor {} does not match actor URI {}", actor.getAcct(), actorId);
            return;
        }

        // Update account fields
        if (actorNode.has("name")) {
            actor.setDisplayName(actorNode.get("name").asText());
        }
        if (actorNode.has("summary")) {
            actor.setNote(actorNode.get("summary").asText());
        }
        if (actorNode.has("preferredUsername")) {
            // Usually shouldn't change username, but some instances allow it
            String newUsername = actorNode.get("preferredUsername").asText();
            if (!newUsername.equals(actor.getUsername())) {
                log.info("Actor {} changed username from {} to {}", 
                        actor.getAcct(), actor.getUsername(), newUsername);
                actor.setUsername(newUsername);
                // Update acct as well
                if (actor.getDomain() != null) {
                    actor.setAcct(newUsername + "@" + actor.getDomain());
                }
            }
        }
        if (actorNode.has("icon") && actorNode.get("icon").isObject()) {
            JsonNode icon = actorNode.get("icon");
            if (icon.has("url")) {
                actor.setAvatarUrl(icon.get("url").asText());
            }
        }
        if (actorNode.has("image") && actorNode.get("image").isObject()) {
            JsonNode image = actorNode.get("image");
            if (image.has("url")) {
                actor.setHeaderUrl(image.get("url").asText());
            }
        }

        // Handle public key update
        if (actorNode.has("publicKey") && actorNode.get("publicKey").isObject()) {
            JsonNode publicKey = actorNode.get("publicKey");
            if (publicKey.has("publicKeyPem")) {
                actor.setPublicKeyPem(publicKey.get("publicKeyPem").asText());
            }
        }

        accountService.save(actor);
        log.info("Updated actor: {}", actor.getAcct());
    }

    private void handleNoteUpdate(JsonNode noteNode, Account actor) {
        String noteId = noteNode.has("id") ? noteNode.get("id").asText() : null;
        if (noteId == null) {
            return;
        }

        // Find the existing status
        Optional<Status> statusOpt = statusService.findByUri(noteId);
        if (statusOpt.isEmpty()) {
            log.debug("Status not found for Update: {}", noteId);
            return;
        }

        Status status = statusOpt.get();

        // Verify ownership
        if (!status.getAccount().getId().equals(actor.getId())) {
            log.warn("Actor {} attempted to update status owned by {}", 
                    actor.getAcct(), status.getAccount().getAcct());
            return;
        }

        // Update status fields
        if (noteNode.has("content")) {
            status.setContent(noteNode.get("content").asText());
        }
        if (noteNode.has("sensitive")) {
            status.setSensitive(noteNode.get("sensitive").asBoolean());
        }
        if (noteNode.has("summary") || noteNode.has("name")) {
            String summary = noteNode.has("summary") ? 
                    noteNode.get("summary").asText() : 
                    noteNode.get("name").asText();
            status.setSpoilerText(summary);
        }

        statusService.save(status);
        log.info("Updated status: {}", noteId);
    }
}
