package org.joinmastodon.web.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class ScopeUtil {
    private ScopeUtil() {
    }

    public static Set<String> parse(String scopes) {
        if (scopes == null || scopes.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(scopes.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static String normalize(String scopes) {
        return String.join(" ", parse(scopes));
    }

    public static String resolveRequestedScopes(String requested, String allowed, String fallback) {
        Set<String> allowedSet = parse(allowed);
        Set<String> requestedSet = parse(requested);

        if (requestedSet.isEmpty()) {
            if (!allowedSet.isEmpty()) {
                return String.join(" ", allowedSet);
            }
            return fallback;
        }

        if (!allowedSet.isEmpty() && !allowedSet.containsAll(requestedSet)) {
            throw new IllegalArgumentException("invalid_scope");
        }

        return String.join(" ", requestedSet);
    }
}
