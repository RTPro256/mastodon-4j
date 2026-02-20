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
 * Handles incoming Delete activities from remote instances.
 * Used to delete remote statuses, accounts, or other objects.
 */
@Service
public class DeleteActivityHandler implements ActivityHandler {
    private static final Logger log = LoggerFactory.getLogger(DeleteActivityHandler.class);

    private final StatusService statusService;
    private final AccountService accountService;

    public DeleteActivityHandler(StatusService statusService, AccountService accountService) {
        this.statusService = statusService;
        this.accountService = accountService;
    }

    @Override
    public void handle(JsonNode activityJson, Account actor, Account localRecipient) {
        if (actor == null) {
            log.warn("Cannot process Delete activity without actor");
            return;
        }

        // Get the object being deleted
        String objectId = activityJson.has("object") ? activityJson.get("object").asText() : null;
        if (objectId == null) {
            log.warn("Delete activity missing object");
            return;
        }

        log.info("Processing Delete from {}: {}", actor.getAcct(), objectId);

        // Try to determine what type of object is being deleted
        // The object could be a string (just the ID) or an object with type info
        JsonNode objectNode = activityJson.get("object");
        String objectType = null;
        
        if (objectNode != null && objectNode.isObject()) {
            objectType = objectNode.has("type") ? objectNode.get("type").asText() : null;
        }

        // Handle based on object type or URL pattern
        if ("Tombstone".equals(objectType)) {
            // Tombstone indicates a deleted object
            handleTombstoneDelete(objectNode, actor);
        } else if (objectId.contains("/statuses/") || objectId.contains("/notes/") || objectId.contains("/objects/")) {
            // Likely a status deletion
            handleStatusDelete(objectId, actor);
        } else if (objectId.contains("/users/") || objectId.contains("/actor/") || objectId.contains("/profile/")) {
            // Likely an account deletion
            handleAccountDelete(objectId, actor);
        } else {
            // Try status first, then account
            if (!handleStatusDelete(objectId, actor)) {
                handleAccountDelete(objectId, actor);
            }
        }
    }

    private void handleTombstoneDelete(JsonNode tombstone, Account actor) {
        String tombstoneId = tombstone.has("id") ? tombstone.get("id").asText() : null;
        if (tombstoneId == null) {
            return;
        }

        // Try to delete as status
        Optional<Status> statusOpt = statusService.findByUri(tombstoneId);
        if (statusOpt.isPresent()) {
            Status status = statusOpt.get();
            // Verify ownership
            if (status.getAccount().getId().equals(actor.getId())) {
                statusService.delete(status);
                log.info("Deleted status via Tombstone: {}", tombstoneId);
            }
        }
    }

    private boolean handleStatusDelete(String statusUri, Account actor) {
        Optional<Status> statusOpt = statusService.findByUri(statusUri);
        if (statusOpt.isEmpty()) {
            return false;
        }

        Status status = statusOpt.get();

        // Verify the actor owns this status
        if (!status.getAccount().getId().equals(actor.getId())) {
            log.warn("Actor {} attempted to delete status owned by {}", 
                    actor.getAcct(), status.getAccount().getAcct());
            return true; // Status exists but actor doesn't own it
        }

        // Delete the status
        statusService.delete(status);
        log.info("Deleted status: {}", statusUri);
        return true;
    }

    private void handleAccountDelete(String accountUri, Account actor) {
        // Verify the actor matches the account being deleted
        if (!accountUri.equals(actor.getActorUri())) {
            log.warn("Actor {} attempted to delete account {}", actor.getAcct(), accountUri);
            return;
        }

        // Find the account
        Optional<Account> accountOpt = accountService.findByActorUri(accountUri);
        if (accountOpt.isEmpty()) {
            log.debug("Account not found for deletion: {}", accountUri);
            return;
        }

        Account account = accountOpt.get();

        // Mark account as deleted/suspended rather than actually deleting
        // This preserves referential integrity
        account.setDisplayName(null);
        account.setNote(null);
        account.setAvatarUrl(null);
        account.setHeaderUrl(null);
        account.setLocked(true);
        
        accountService.save(account);
        log.info("Marked account as deleted: {}", accountUri);

        // Note: In a full implementation, we would also:
        // - Delete or tombstone all statuses
        // - Remove follow relationships
        // - Clean up media attachments
        // - Send Delete activities to followers
    }
}
