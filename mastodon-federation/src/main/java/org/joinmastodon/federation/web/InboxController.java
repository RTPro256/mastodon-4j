package org.joinmastodon.federation.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.joinmastodon.activitypub.model.ActivityType;
import org.joinmastodon.activitypub.signature.HttpSignature;
import org.joinmastodon.activitypub.signature.HttpSignatureVerifier;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.federation.service.ActivityDispatcher;
import org.joinmastodon.federation.service.RemoteActorService;
import org.joinmastodon.federation.config.FederationProperties;
import org.joinmastodon.activitypub.signature.PemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for receiving ActivityPub activities.
 * Handles both user-specific inboxes and the shared inbox.
 */
@RestController
@RequestMapping
public class InboxController {
    private static final Logger log = LoggerFactory.getLogger(InboxController.class);

    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final RemoteActorService remoteActorService;
    private final ActivityDispatcher activityDispatcher;
    private final HttpSignatureVerifier signatureVerifier;
    private final FederationProperties properties;

    public InboxController(ObjectMapper objectMapper,
                          AccountService accountService,
                          RemoteActorService remoteActorService,
                          ActivityDispatcher activityDispatcher,
                          HttpSignatureVerifier signatureVerifier,
                          FederationProperties properties) {
        this.objectMapper = objectMapper;
        this.accountService = accountService;
        this.remoteActorService = remoteActorService;
        this.activityDispatcher = activityDispatcher;
        this.signatureVerifier = signatureVerifier;
        this.properties = properties;
    }

    /**
     * Shared inbox endpoint for receiving activities addressed to any local user.
     * More efficient for delivery of activities to multiple local recipients.
     *
     * @param requestBody the raw activity JSON
     * @param request the HTTP request (for signature verification)
     * @return 202 Accepted on success
     */
    @PostMapping(value = "/inbox", consumes = {
            "application/activity+json",
            "application/ld+json",
            "application/json"
    })
    public ResponseEntity<Void> sharedInbox(@RequestBody JsonNode requestBody,
                                            HttpServletRequest request) {
        return processInboxRequest(requestBody, request, null);
    }

    /**
     * User-specific inbox endpoint for receiving activities addressed to a specific user.
     *
     * @param username the local username
     * @param requestBody the raw activity JSON
     * @param request the HTTP request (for signature verification)
     * @return 202 Accepted on success
     */
    @PostMapping(value = "/users/{username}/inbox", consumes = {
            "application/activity+json",
            "application/ld+json",
            "application/json"
    })
    public ResponseEntity<Void> userInbox(@PathVariable String username,
                                          @RequestBody JsonNode requestBody,
                                          HttpServletRequest request) {
        // Verify the local user exists
        Optional<Account> localAccount = accountService.findLocalAccountByUsername(username);
        if (localAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return processInboxRequest(requestBody, request, localAccount.get());
    }

    /**
     * Process an incoming activity.
     * Verifies the HTTP signature and dispatches to the appropriate handler.
     */
    private ResponseEntity<Void> processInboxRequest(JsonNode requestBody,
                                                     HttpServletRequest request,
                                                     Account localRecipient) {
        try {
            // Extract activity type
            String typeStr = requestBody.has("type") ? requestBody.get("type").asText() : null;
            ActivityType activityType = ActivityType.fromString(typeStr);

            if (activityType == null) {
                log.warn("Received unknown activity type: {}", typeStr);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Get the actor URL
            String actorUrl = requestBody.has("actor") ? requestBody.get("actor").asText() : null;
            if (actorUrl == null) {
                log.warn("Received activity without actor");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Verify HTTP signature if required
            if (properties.isRequireSignatures()) {
                if (!verifySignature(request, actorUrl)) {
                    log.warn("HTTP signature verification failed for actor: {}", actorUrl);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }

            // Fetch or get the remote actor
            Account actor = remoteActorService.fetchAndStore(actorUrl)
                    .orElse(null);

            if (actor == null) {
                log.warn("Could not fetch remote actor: {}", actorUrl);
                // Still process the activity - some instances may not have accessible actor profiles
            }

            // Dispatch to appropriate handler
            activityDispatcher.dispatch(requestBody, activityType, actor, localRecipient);

            // Return 202 Accepted (ActivityPub spec)
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();

        } catch (Exception e) {
            log.error("Error processing inbox activity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verify the HTTP signature on an incoming request.
     */
    private boolean verifySignature(HttpServletRequest request, String actorUrl) {
        try {
            // Extract the Signature header
            String signatureHeader = request.getHeader("Signature");
            if (signatureHeader == null || signatureHeader.isBlank()) {
                log.debug("No Signature header present");
                return false;
            }

            // Parse the signature
            HttpSignature signature = HttpSignature.parse(signatureHeader);
            if (signature == null) {
                log.debug("Could not parse signature header");
                return false;
            }

            // Get the actor's public key
            Account actor = remoteActorService.fetchAndStore(actorUrl).orElse(null);
            if (actor == null || actor.getPublicKeyPem() == null) {
                log.debug("Could not fetch actor or public key for: {}", actorUrl);
                return false;
            }

            // Parse the public key
            PublicKey publicKey = PemUtils.parsePublicKey(actor.getPublicKeyPem());

            // Build headers map from request
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                headers.put(name.toLowerCase(), request.getHeader(name));
            }

            // Add digest header if present
            String digest = request.getHeader("Digest");
            if (digest != null) {
                headers.put("digest", digest);
            }

            // Verify the signature
            String method = request.getMethod().toLowerCase();
            String path = request.getRequestURI();

            return signatureVerifier.verify(signature, publicKey, method, path, headers);

        } catch (Exception e) {
            log.error("Error verifying HTTP signature", e);
            return false;
        }
    }
}
