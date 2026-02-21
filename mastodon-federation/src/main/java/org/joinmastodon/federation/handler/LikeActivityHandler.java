package org.joinmastodon.federation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Favourite;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.service.FavouriteService;
import org.joinmastodon.core.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Handles incoming Like (favorite) activities from remote instances.
 * Represents a favorite of a status by a remote user.
 */
@Service
public class LikeActivityHandler implements ActivityHandler {
    private static final Logger log = LoggerFactory.getLogger(LikeActivityHandler.class);

    private final StatusService statusService;
    private final FavouriteService favouriteService;

    public LikeActivityHandler(StatusService statusService, FavouriteService favouriteService) {
        this.statusService = statusService;
        this.favouriteService = favouriteService;
    }

    @Override
    public void handle(JsonNode activityJson, Account actor, Account localRecipient) {
        if (actor == null) {
            log.warn("Cannot process Like activity without actor");
            return;
        }

        // Get the object being liked
        String objectUrl = activityJson.has("object") ? activityJson.get("object").asText() : null;
        if (objectUrl == null) {
            log.warn("Like activity missing object");
            return;
        }

        log.info("Processing Like from {}: {}", actor.getAcct(), objectUrl);

        // Find the status
        Optional<Status> statusOpt = statusService.findByUri(objectUrl);
        if (statusOpt.isEmpty()) {
            log.debug("Status not found for Like: {}", objectUrl);
            return;
        }

        Status status = statusOpt.get();

        // Check if this favorite already exists
        Optional<Favourite> existingFavourite = favouriteService.findByAccountAndStatus(actor, status);
        if (existingFavourite.isPresent()) {
            log.debug("Favorite already exists: {} -> {}", actor.getAcct(), objectUrl);
            return;
        }

        // Create the favorite
        Favourite favourite = new Favourite();
        favourite.setAccount(actor);
        favourite.setStatus(status);
        favourite.setCreatedAt(Instant.now());

        favouriteService.save(favourite);
        log.info("Created favorite: {} -> {}", actor.getAcct(), objectUrl);
    }
}
