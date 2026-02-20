package org.joinmastodon.web.auth;

import java.util.Set;
import org.joinmastodon.core.entity.User;

public record AuthenticatedPrincipal(
        Long userId,
        Long accountId,
        Long applicationId,
        Set<String> scopes,
        User.Role role) {
}
