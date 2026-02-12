package org.joinmastodon.activitypub.signature;

import java.util.LinkedHashMap;
import java.util.Map;

final class HeaderParser {
    private HeaderParser() {
    }

    static Map<String, String> parseHeader(String headerValue) {
        Map<String, String> result = new LinkedHashMap<>();
        if (headerValue == null || headerValue.isBlank()) {
            return result;
        }
        String[] parts = headerValue.split(",");
        for (String part : parts) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].trim();
            String value = kv[1].trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            result.put(key, value);
        }
        return result;
    }
}
