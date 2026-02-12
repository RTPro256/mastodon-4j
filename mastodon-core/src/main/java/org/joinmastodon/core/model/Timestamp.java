package org.joinmastodon.core.model;

import java.time.Instant;
import java.util.Objects;

public record Timestamp(Instant value) {
    public Timestamp {
        Objects.requireNonNull(value, "value");
    }

    public static Timestamp now() {
        return new Timestamp(Instant.now());
    }
}
