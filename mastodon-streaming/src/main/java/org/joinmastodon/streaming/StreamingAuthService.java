package org.joinmastodon.streaming;

import java.time.Instant;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.joinmastodon.core.service.OAuthAccessTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StreamingAuthService {
    private final OAuthAccessTokenService accessTokenService;

    public StreamingAuthService(OAuthAccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    public Long requireAccountId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String tokenValue = authorizationHeader.substring("Bearer ".length()).trim();
        if (tokenValue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        OAuthAccessToken token = accessTokenService.findByToken(tokenValue)
                .filter(t -> !t.isRevoked())
                .filter(t -> !t.isExpired(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (token.getUser() == null || token.getUser().getAccount() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return token.getUser().getAccount().getId();
    }
}
