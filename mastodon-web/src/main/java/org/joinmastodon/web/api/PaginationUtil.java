package org.joinmastodon.web.api;

import java.util.List;

public final class PaginationUtil {
    private PaginationUtil() {
    }

    public static Long nextMaxId(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids.stream().min(Long::compareTo).orElse(null);
    }

    public static Long prevSinceId(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids.stream().max(Long::compareTo).orElse(null);
    }
}
