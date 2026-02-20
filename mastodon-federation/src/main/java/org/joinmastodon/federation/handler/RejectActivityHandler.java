package org.joinmastodon.federation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Follow;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.FollowService;
import org.joinmastodon.federation.config.FederationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles incoming Reject activities from remote instances.
 * Typically sent when a follow request is rejected by a remote user.
 */
@Service
public class RejectActivityHandler implements ActivityHandler {
    private static final Logger log = LoggerFactory.getLogger(RejectActivityHandler.class);

    private final AccountService accountService;
    private final FollowService followService;
    private final FederationProperties properties;

    public RejectActivityHandler(AccountService accountService,
                                 FollowService followService,
                                 FederationProperties properties) {
        this.accountService = accountService;
        this.followService = followService;
        this.properties = properties;
    }

    @Override
    public void handle(JsonNode activityJson, Account actor, Account localRecipient) {
        if (actor == null) {
            log.warn("Cannot process Reject activity without actor");
            return;
        }

        // Get the object being rejected (usually a Follow activity)
        JsonNode objectNode = activityJson.get("object");
        if (objectNode == null || !objectNode.isObject()) {
            log.warn("Reject activity missing object");
            return;
        }

        // Extract the original Follow activity details
        String followActor = objectNode.has("actor") ? objectNode.get("actor").asText() : null;
        String followObject = objectNode.has("object") ? objectNode.get("object").asText() : null;

        if (followActor == null || followObject == null) {
            log.warn("Reject activity object missing Follow details");
            return;
        }

        // Verify the follow was from a local user
        String baseUrl = properties.getBaseUrl().replaceAll("/$", "");
        if (!followActor.startsWith(baseUrl)) {
            log.warn("Reject activity for non-local follow actor: {}", followActor);
            return;
        }

        // Extract local username from actor URL
        String localUsername = followActor.substring((baseUrl + "/users/").length());
        int slashIndex = localUsername.indexOf('/');
        if (slashIndex > 0) {
            localUsername = localUsername.substring(0, slashIndex);
        }

        Optional<Account> localAccountOpt = accountService.findLocalAccountByUsername(localUsername);
        if (localAccountOpt.isEmpty()) {
            log.warn("Local account not found for Reject: {}", localUsername);
            return;
        }

        Account localAccount = localAccountOpt.get();

        // Verify the follow target matches the actor
        if (!followObject.equals(actor.getActorUri())) {
            log.warn("Reject actor {} does not match follow target {}", actor.getActorUri(), followObject);
            return;
        }

        log.info("Processing Reject from {} for follow by {}", actor.getAcct(), localAccount.getAcct());

        // Remove any pending follow request
        Optional<Follow> existingFollow = followService.findByAccountAndTarget(localAccount, actor);
        if (existingFollow.isPresent()) {
            followService.unfollow(localAccount, actor);
            log.info("Removed follow relationship: {} -> {}", localAccount.getAcct(), actor.getAcct());
        } else {
            log.debug("No follow relationship to remove between {} and {}", 
                    localAccount.getAcct(), actor.getAcct());
        }
    }
}
