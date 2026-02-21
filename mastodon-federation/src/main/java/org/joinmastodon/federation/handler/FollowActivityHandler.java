package org.joinmastodon.federation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.activitypub.model.AcceptActivity;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Follow;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.FollowService;
import org.joinmastodon.federation.config.FederationProperties;
import org.joinmastodon.federation.service.FederationDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handles incoming Follow activities from remote instances.
 * Creates a follow relationship and sends an Accept response.
 */
@Service
public class FollowActivityHandler implements ActivityHandler {
    private static final Logger log = LoggerFactory.getLogger(FollowActivityHandler.class);

    private final AccountService accountService;
    private final FollowService followService;
    private final FederationDeliveryService deliveryService;
    private final FederationProperties properties;

    public FollowActivityHandler(AccountService accountService,
                                 FollowService followService,
                                 FederationDeliveryService deliveryService,
                                 FederationProperties properties) {
        this.accountService = accountService;
        this.followService = followService;
        this.deliveryService = deliveryService;
        this.properties = properties;
    }

    @Override
    public void handle(JsonNode activityJson, Account actor, Account localRecipient) {
        if (actor == null) {
            log.warn("Cannot process Follow activity without actor");
            return;
        }

        // Get the target (object) of the follow
        String targetUrl = activityJson.has("object") ? activityJson.get("object").asText() : null;
        if (targetUrl == null) {
            log.warn("Follow activity missing object (target)");
            return;
        }

        // Parse the target URL to extract username
        // Expected format: https://domain/users/username
        Account targetAccount = resolveTargetAccount(targetUrl);
        if (targetAccount == null) {
            log.warn("Could not resolve target account for Follow: {}", targetUrl);
            return;
        }

        // Verify target is a local account
        if (!targetAccount.isLocalAccount()) {
            log.warn("Follow target is not a local account: {}", targetUrl);
            return;
        }

        log.info("Processing Follow from {} to {}", actor.getAcct(), targetAccount.getAcct());

        // Create the follow relationship
        Follow follow = followService.follow(actor, targetAccount);

        // For unlocked accounts, auto-accept the follow
        if (!targetAccount.isLocked()) {
            sendAccept(activityJson, actor, targetAccount, follow);
        } else {
            // For locked accounts, the follow request is pending
            // The user must manually approve it
            log.info("Follow request from {} to {} is pending approval", 
                    actor.getAcct(), targetAccount.getAcct());
        }
    }

    private Account resolveTargetAccount(String targetUrl) {
        // Try to extract username from URL
        // Format: https://domain/users/username
        String baseUrl = properties.getBaseUrl().replaceAll("/$", "");
        
        if (targetUrl.startsWith(baseUrl + "/users/")) {
            String username = targetUrl.substring((baseUrl + "/users/").length());
            // Remove any trailing path components
            int slashIndex = username.indexOf('/');
            if (slashIndex > 0) {
                username = username.substring(0, slashIndex);
            }
            return accountService.findLocalAccountByUsername(username).orElse(null);
        }
        
        // Try by actor URI
        return accountService.findByActorUri(targetUrl).orElse(null);
    }

    private void sendAccept(JsonNode followActivity, Account actor, Account targetAccount, Follow follow) {
        try {
            // Create Accept activity
            String baseUrl = properties.getBaseUrl().replaceAll("/$", "");
            String acceptId = baseUrl + "/activities/accept/" + follow.getId();
            
            AcceptActivity accept = new AcceptActivity(
                    baseUrl + "/users/" + targetAccount.getUsername(),
                    followActivity
            );
            accept.setId(acceptId);

            // Deliver to the actor's inbox
            String inboxUrl = actor.getInboxUrl();
            if (inboxUrl != null) {
                deliveryService.deliver(accept, inboxUrl, targetAccount);
                log.info("Sent Accept for Follow from {} to {}", targetAccount.getAcct(), actor.getAcct());
            }
        } catch (Exception e) {
            log.error("Failed to send Accept activity", e);
        }
    }
}
