package org.joinmastodon.core.entity;

public enum NotificationType {
    MENTION,
    STATUS,
    REBLOG,
    FOLLOW,
    FAVOURITE,
    POLL,
    UPDATE

    ;

    public String toApiValue() {
        return name().toLowerCase();
    }
}
