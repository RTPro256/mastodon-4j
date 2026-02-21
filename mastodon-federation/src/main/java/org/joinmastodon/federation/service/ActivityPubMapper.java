package org.joinmastodon.federation.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.joinmastodon.activitypub.model.Actor;
import org.joinmastodon.activitypub.model.Create;
import org.joinmastodon.activitypub.model.MediaLink;
import org.joinmastodon.activitypub.model.Note;
import org.joinmastodon.activitypub.model.OrderedCollection;
import org.joinmastodon.activitypub.model.PublicKey;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.federation.config.FederationProperties;
import org.springframework.stereotype.Service;

@Service
public class ActivityPubMapper {
    private static final List<String> PUBLIC_TO = List.of("https://www.w3.org/ns/activitystreams#Public");

    private final FederationProperties properties;
    private final FederationKeyService keyService;

    public ActivityPubMapper(FederationProperties properties, FederationKeyService keyService) {
        this.properties = properties;
        this.keyService = keyService;
    }

    public Actor toActor(Account account) {
        Actor actor = new Actor();
        String baseUrl = properties.getBaseUrl().replaceAll("/$", "");
        String actorId = baseUrl + "/users/" + account.getUsername();
        actor.setId(actorId);
        actor.setPreferredUsername(account.getUsername());
        actor.setName(account.getDisplayName());
        actor.setSummary(account.getNote());
        actor.setInbox(actorId + "/inbox");
        actor.setOutbox(actorId + "/outbox");
        actor.setFollowers(actorId + "/followers");
        actor.setFollowing(actorId + "/following");
        actor.setUrl(actorId);
        if (account.getAvatarUrl() != null) {
            actor.setIcon(new MediaLink("Image", null, account.getAvatarUrl()));
        }
        if (account.getHeaderUrl() != null) {
            actor.setImage(new MediaLink("Image", null, account.getHeaderUrl()));
        }
        String keyId = keyService.getFederationKeys().getKeyId();
        actor.setPublicKey(new PublicKey(keyId, actorId, keyService.getPublicKeyPem()));
        actor.setEndpoints(Map.of("sharedInbox", baseUrl + "/inbox"));
        return actor;
    }

    public Note toNote(Status status) {
        Note note = new Note();
        String baseUrl = properties.getBaseUrl().replaceAll("/$", "");
        String actorId = baseUrl + "/users/" + status.getAccount().getUsername();
        String id = status.getUri() != null ? status.getUri() : baseUrl + "/users/" + status.getAccount().getUsername() + "/statuses/" + status.getId();
        note.setId(id);
        note.setAttributedTo(actorId);
        note.setContent(status.getContent());
        note.setPublished(status.getCreatedAt() == null ? Instant.now() : status.getCreatedAt());
        note.setTo(PUBLIC_TO);
        return note;
    }

    public Create toCreateActivity(Status status) {
        Create activity = new Create();
        String baseUrl = properties.getBaseUrl().replaceAll("/$", "");
        String actorId = baseUrl + "/users/" + status.getAccount().getUsername();
        String id = baseUrl + "/activities/" + status.getId();
        activity.setId(id);
        activity.setActor(actorId);
        activity.setObject(toNote(status));
        activity.setPublished(status.getCreatedAt() == null ? Instant.now() : status.getCreatedAt());
        activity.setTo(PUBLIC_TO);
        return activity;
    }

    public OrderedCollection<Create> toOutbox(Account account, List<Status> statuses) {
        OrderedCollection<Create> collection = new OrderedCollection<>();
        String baseUrl = properties.getBaseUrl().replaceAll("/$", "");
        collection.setId(baseUrl + "/users/" + account.getUsername() + "/outbox");
        collection.setTotalItems(statuses.size());
        collection.setOrderedItems(statuses.stream().map(this::toCreateActivity).toList());
        return collection;
    }
}
