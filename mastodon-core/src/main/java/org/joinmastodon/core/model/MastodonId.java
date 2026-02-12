package org.joinmastodon.core.model;

public record MastodonId(long value) {
    public MastodonId {
        if (value <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
