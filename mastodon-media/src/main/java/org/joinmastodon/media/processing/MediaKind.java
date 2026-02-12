package org.joinmastodon.media.processing;

public enum MediaKind {
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    UNKNOWN("unknown");

    private final String apiValue;

    MediaKind(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static MediaKind fromContentType(String contentType) {
        if (contentType == null) {
            return UNKNOWN;
        }
        String lower = contentType.toLowerCase();
        if (lower.startsWith("image/")) {
            return IMAGE;
        }
        if (lower.startsWith("video/")) {
            return VIDEO;
        }
        if (lower.startsWith("audio/")) {
            return AUDIO;
        }
        return UNKNOWN;
    }
}
