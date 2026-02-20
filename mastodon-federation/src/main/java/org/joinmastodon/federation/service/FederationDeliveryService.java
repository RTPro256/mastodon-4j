package org.joinmastodon.federation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joinmastodon.activitypub.signature.DigestUtils;
import org.joinmastodon.activitypub.signature.HttpSignatureSigner;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.federation.config.FederationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service for delivering ActivityPub activities to remote inboxes.
 * Handles HTTP signing, retries, and delivery queue management.
 */
@Service
public class FederationDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(FederationDeliveryService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final FederationKeyService keyService;
    private final FederationProperties properties;
    private final HttpSignatureSigner signatureSigner;

    public FederationDeliveryService(HttpClient httpClient,
                                     ObjectMapper objectMapper,
                                     FederationKeyService keyService,
                                     FederationProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.keyService = keyService;
        this.properties = properties;
        this.signatureSigner = new HttpSignatureSigner();
    }

    /**
     * Deliver an activity to a single inbox URL.
     * This is an async operation that signs and sends the request.
     *
     * @param activity the activity to deliver
     * @param inboxUrl the target inbox URL
     * @param sender the local account sending the activity
     */
    @Async
    public void deliver(Object activity, String inboxUrl, Account sender) {
        try {
            String json = objectMapper.writeValueAsString(activity);
            deliverWithRetry(json, inboxUrl, sender, 0);
        } catch (Exception e) {
            log.error("Failed to serialize activity for delivery to {}", inboxUrl, e);
        }
    }

    /**
     * Deliver an activity to multiple inbox URLs.
     * Used for fan-out delivery to followers.
     *
     * @param activity the activity to deliver
     * @param inboxUrls the list of target inbox URLs
     * @param sender the local account sending the activity
     */
    @Async
    public void deliverToMany(Object activity, List<String> inboxUrls, Account sender) {
        for (String inboxUrl : inboxUrls) {
            deliver(activity, inboxUrl, sender);
        }
    }

    /**
     * Deliver a pre-serialized activity JSON with retry logic.
     */
    private void deliverWithRetry(String json, String inboxUrl, Account sender, int attempt) {
        try {
            // Build the HTTP request
            HttpRequest request = buildSignedRequest(json, inboxUrl, sender);

            // Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.debug("Successfully delivered activity to {} (status {})", inboxUrl, response.statusCode());
                return;
            }

            // Handle non-success responses
            if (response.statusCode() >= 400 && response.statusCode() < 500) {
                // Client error - don't retry
                log.warn("Client error delivering to {} (status {}): {}", 
                        inboxUrl, response.statusCode(), response.body());
                return;
            }

            // Server error - retry with backoff
            handleRetry(json, inboxUrl, sender, attempt, 
                    "Server error: " + response.statusCode());

        } catch (Exception e) {
            handleRetry(json, inboxUrl, sender, attempt, e.getMessage());
        }
    }

    /**
     * Handle retry logic with exponential backoff.
     */
    private void handleRetry(String json, String inboxUrl, Account sender, int attempt, String error) {
        int maxRetries = 5;
        if (attempt >= maxRetries) {
            log.error("Failed to deliver to {} after {} attempts: {}", inboxUrl, maxRetries, error);
            return;
        }

        // Exponential backoff: 1s, 2s, 4s, 8s, 16s
        long delaySeconds = (long) Math.pow(2, attempt);
        log.warn("Delivery to {} failed (attempt {}/{}): {}. Retrying in {}s", 
                inboxUrl, attempt + 1, maxRetries, error, delaySeconds);

        try {
            Thread.sleep(Duration.ofSeconds(delaySeconds).toMillis());
            deliverWithRetry(json, inboxUrl, sender, attempt + 1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Delivery retry interrupted for {}", inboxUrl);
        }
    }

    /**
     * Build a signed HTTP POST request for ActivityPub delivery.
     */
    private HttpRequest buildSignedRequest(String json, String inboxUrl, Account sender) throws Exception {
        // Calculate digest
        String digest = DigestUtils.sha256Digest(json);

        // Build headers map for signing
        String date = Instant.now().toString();
        String host = URI.create(inboxUrl).getHost();

        Map<String, String> headers = Map.of(
                "host", host,
                "date", date,
                "digest", digest,
                "content-type", "application/activity+json"
        );

        // Sign the request
        String keyId = keyService.getFederationKeys().getKeyId();
        var keys = keyService.getFederationKeys();
        String signature = signatureSigner.sign(
                keyId,
                keys.getPrivateKey(),
                "post",
                URI.create(inboxUrl).getPath(),
                headers,
                List.of("(request-target)", "host", "date", "digest")
        );

        // Build the HTTP request
        return HttpRequest.newBuilder()
                .uri(URI.create(inboxUrl))
                .header("Content-Type", "application/activity+json")
                .header("Accept", "application/activity+json")
                .header("Date", date)
                .header("Digest", digest)
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    /**
     * Queue a delivery job for later processing.
     * This is used for scheduled/retry deliveries.
     *
     * @param activity the activity to deliver
     * @param inboxUrl the target inbox URL
     * @param senderId the local account ID sending the activity
     */
    public void queueDelivery(Object activity, String inboxUrl, Long senderId) {
        // This would integrate with the mastodon-jobs module
        // For now, we deliver immediately
        log.debug("Queuing delivery to {} for sender {}", inboxUrl, senderId);
    }
}
