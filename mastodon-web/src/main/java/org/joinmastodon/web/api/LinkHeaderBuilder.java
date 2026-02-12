package org.joinmastodon.web.api;

import java.util.ArrayList;
import java.util.List;

public final class LinkHeaderBuilder {
    private LinkHeaderBuilder() {
    }

    public static String build(String basePath, Long nextMaxId, Long prevSinceId) {
        List<String> links = new ArrayList<>();
        String separator = basePath.contains("?") ? "&" : "?";
        if (nextMaxId != null) {
            links.add('<' + basePath + separator + "max_id=" + nextMaxId + ">; rel=\"next\"");
        }
        if (prevSinceId != null) {
            links.add('<' + basePath + separator + "since_id=" + prevSinceId + ">; rel=\"prev\"");
        }
        return links.isEmpty() ? null : String.join(", ", links);
    }
}
