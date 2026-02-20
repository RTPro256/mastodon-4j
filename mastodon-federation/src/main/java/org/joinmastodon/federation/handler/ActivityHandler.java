package org.joinmastodon.federation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Account;

/**
 * Base interface for ActivityPub activity handlers.
 * All activity handlers must implement this interface.
 */
public interface ActivityHandler {

    /**
     * Handle an incoming ActivityPub activity.
     *
     * @param activityJson the raw activity JSON
     * @param actor the remote actor who sent the activity (may be null if fetch failed)
     * @param localRecipient the local recipient (for user-specific inbox, null for shared inbox)
     */
    void handle(JsonNode activityJson, Account actor, Account localRecipient);
}
