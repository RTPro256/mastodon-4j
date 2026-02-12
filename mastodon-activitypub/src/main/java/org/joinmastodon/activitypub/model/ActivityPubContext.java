package org.joinmastodon.activitypub.model;

import java.util.List;

public final class ActivityPubContext {
    public static final List<Object> DEFAULT = List.of(
            "https://www.w3.org/ns/activitystreams",
            "https://w3id.org/security/v1"
    );

    private ActivityPubContext() {
    }
}
