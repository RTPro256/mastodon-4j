package org.joinmastodon.web.auth;

import java.util.Set;

public record AuthenticatedPrincipal(
        Long userId,
        Long accountId,
        Long applicationId,
        Set<String> scopes) {
}
