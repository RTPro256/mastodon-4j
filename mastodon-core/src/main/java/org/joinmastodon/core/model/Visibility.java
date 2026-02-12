package org.joinmastodon.core.model;

public enum Visibility {
    PUBLIC,
    UNLISTED,
    PRIVATE,
    DIRECT;

    public String toApiValue() {
        return name().toLowerCase();
    }

    public static Visibility fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Visibility.valueOf(value.trim().toUpperCase());
    }
}
